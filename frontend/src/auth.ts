const TOKEN_KEY = "cloudbank.accessToken";

export function token(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function saveToken(value: string): void {
  sessionStorage.setItem(TOKEN_KEY, value);
}

export function logout(): void {
  sessionStorage.removeItem(TOKEN_KEY);
}

export function authenticated(): boolean {
  return Boolean(token());
}
