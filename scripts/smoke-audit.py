#!/usr/bin/env python3
"""Publish a synthetic event twice and verify one persisted audit record."""
import datetime
import json
import subprocess
import time
import uuid

identifier = str(uuid.uuid4())
event = json.dumps({'eventId': identifier, 'eventType':'USER_CONTACT_REGISTERED', 'eventVersion':1,
                   'aggregateType':'AUTH_USER', 'aggregateId':identifier,
                   'occurredAt':datetime.datetime.now(datetime.timezone.utc).isoformat(), 'data':{'userId':identifier, 'email':f'audit-{identifier}@example.test', 'firstName':'Audit', 'lastName':'Smoke'}})
subprocess.run(['docker', 'exec', '-i', 'cloudbank-kafka', '/opt/kafka/bin/kafka-console-producer.sh',
                '--bootstrap-server', 'localhost:19092', '--topic', 'cloudbank.user-contact.v1'],
               input=event + '\n' + event + '\n', text=True, check=True, capture_output=True, timeout=30)
for attempt in range(30):
    result = subprocess.run(['docker', 'exec', 'cloudbank-postgres', 'psql', '-U', 'cloudbank_audit',
                             '-d', 'cloudbank_audit', '-Atc',
                             f"SELECT count(*) FROM audit_events WHERE event_id = '{identifier}'"],
                            text=True, capture_output=True, check=True, timeout=10)
    if result.stdout.strip() == '1':
        time.sleep(2)
        again = subprocess.run(result.args, text=True, capture_output=True, check=True, timeout=10)
        assert again.stdout.strip() == '1'
        print('PASS: Kafka audit event persisted once after duplicate delivery')
        break
    time.sleep(1)
else:
    raise AssertionError('Audit event was not persisted within 30 seconds')
