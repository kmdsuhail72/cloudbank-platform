#!/usr/bin/env python3
"""Exercise the local Docker stack. Creates two isolated smoke-test customers."""
import json
import os
import time
import uuid
import urllib.request
import urllib.error

BASE = os.environ.get('CLOUDBANK_URL', 'http://localhost:8080')

def call(method, path, body=None, token=None, expected=200):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(BASE + path, data=None if body is None else json.dumps(body).encode(), headers=headers, method=method)
    try:
        response = urllib.request.urlopen(req, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    raw = response.read()
    assert response.status in (expected if isinstance(expected, tuple) else (expected,)), f'{method} {path}: expected {expected}, got {response.status}: {raw.decode()}'
    return json.loads(raw) if raw else None

def customer():
    identity = {'email': f'smoke-{uuid.uuid4().hex}@example.test', 'password': 'Smoke-' + uuid.uuid4().hex}
    call('POST', '/api/v1/auth/register', identity, expected=202)
    token = call('POST', '/api/v1/auth/login', identity)['accessToken']
    profile = call('GET', '/api/v1/customers/me', token=token, expected=(200, 404))
    if not profile or 'id' not in profile:
        call('POST', '/api/v1/customers/me', {'firstName':'Smoke', 'lastName':'Test', 'phoneNumber':'+15555550123', 'dateOfBirth':'1990-01-01'}, token, expected=(201, 409))
    return token

def main():
    first, second = customer(), customer()
    for route in ['cards', 'beneficiaries', 'loans', 'transactions']:
        call('GET', '/api/v1/' + route, expected=401)
        assert call('GET', '/api/v1/' + route, token=first) == []
    account = call('POST', '/api/v1/accounts', {'accountType':'CHECKING', 'currency':'USD'}, first, 201)
    card = call('POST', '/api/v1/cards', {'cardholderName':'Smoke Test', 'type':'DEBIT', 'currency':'USD'}, first, 201)
    for status in ['BLOCKED', 'ACTIVE']:
        assert call('PATCH', f'/api/v1/cards/{card["id"]}/status', {'status':status}, first)['status'] == status
    call('PATCH', f'/api/v1/cards/{card["id"]}/status', {'status':'INVALID'}, first, 400)
    beneficiary_body = {'nickname':'Test payee', 'accountHolderName':'Test Person', 'accountNumber':'123456789', 'bankName':'Test Bank', 'currency':'USD'}
    beneficiary = call('POST', '/api/v1/beneficiaries', beneficiary_body, first, 201)
    beneficiary_body['nickname'] = 'Updated payee'
    assert call('PUT', f'/api/v1/beneficiaries/{beneficiary["id"]}', beneficiary_body, first)['nickname'] == 'Updated payee'
    loan_body = {'principalAmount':1000, 'annualInterestRate':5, 'termMonths':12, 'currency':'USD', 'status':'APPLICATION'}
    loan = call('POST', '/api/v1/loans', loan_body, first, 201)
    call('PATCH', f'/api/v1/loans/{loan["id"]}/status', {'status':'APPROVED'}, first, 409)
    call('POST', '/api/v1/loans', dict(loan_body, status='APPROVED'), first, 400)
    assert call('PATCH', f'/api/v1/loans/{loan["id"]}/status', {'status':'REJECTED'}, first)['status'] == 'REJECTED'
    tx_body = {'accountId':account['id'], 'amount':12.5, 'currency':'USD', 'description':'Smoke manual record', 'type':'DEBIT', 'status':'POSTED'}
    transaction = call('POST', '/api/v1/transactions', tx_body, first, 201)
    call('POST', '/api/v1/transactions', dict(tx_body, amount=-1), first, 400)
    call('POST', '/api/v1/transactions', dict(tx_body, currency='EUR'), first, 400)
    call('POST', '/api/v1/transactions', tx_body, second, 404)
    assert call('PATCH', f'/api/v1/transactions/{transaction["id"]}/status', {'status':'FAILED'}, first)['status'] == 'FAILED'
    assert len(call('GET', f'/api/v1/transactions/account/{account["id"]}', token=first)) == 1
    history = call('GET', f'/api/v1/accounts/{account["id"]}/transactions', token=first)
    assert isinstance(history, dict) and 'transactions' in history, 'Ledger route was shadowed'
    for route, entity in [('cards',card), ('beneficiaries',beneficiary), ('loans',loan), ('transactions',transaction)]:
        call('GET', f'/api/v1/{route}/{entity["id"]}', token=second, expected=404)
        assert call('GET', '/api/v1/' + route, token=second) == []
    call('DELETE', f'/api/v1/beneficiaries/{beneficiary["id"]}', token=second, expected=404)
    call('DELETE', f'/api/v1/beneficiaries/{beneficiary["id"]}', token=first, expected=204)
    print('PASS: banking CRUD, validation, ownership isolation, loan restrictions, and ledger routing')

if __name__ == '__main__':
    main()
