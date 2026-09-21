import {
  useEffect,
  useState,
  type FormEvent,
  type ReactNode,
} from "react";
import {
  Link,
  Navigate,
  NavLink,
  Route,
  Routes,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { api, ApiError } from "./api";
import {
  authenticated,
  logout,
  saveToken,
} from "./auth";
import type {
  AccountResponse,
  AccountStatus,
  AccountType,
  BeneficiaryResponse,
  CardResponse,
  CardStatus,
  CardType,
  CustomerProfilePayload,
  CustomerProfileResponse,
  LedgerBalanceResult,
  LedgerTransactionPage,
  LoanResponse,
  LoanStatus,
  TransactionResponse,
  TransactionType,
  TransferResult,
} from "./types";

const money = (amount: number, currency: string) =>
  new Intl.NumberFormat(undefined, {
    style: "currency",
    currency,
  }).format(amount);

function Logo() {
  return (
    <div className="logo" aria-label="CloudBank logo">
      <svg viewBox="0 0 32 32" aria-hidden="true">
        <path
          d="M6 13.25 16 7l10 6.25v2.2H6v-2.2Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinejoin="round"
        />
        <path
          d="M8.5 17.5h15M10 17.5v7m4-7v7m4-7v7m4-7v7M7 26h18"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
      </svg>
    </div>
  );
}

function StatIcon({ type }: { type: "accounts" | "profile" | "active" }) {
  return (
    <span className={`stat-icon ${type}`} aria-hidden="true">
      <svg viewBox="0 0 24 24">
        {type === "accounts" && (
          <path d="M4 10.5 12 5l8 5.5M6 11v7m4-7v7m4-7v7m4-7v7M4 19h16" />
        )}
        {type === "profile" && (
          <path d="M12 3.5a4 4 0 1 1 0 8 4 4 0 0 1 0-8ZM4.5 20a7.5 7.5 0 0 1 15 0" />
        )}
        {type === "active" && (
          <path d="m5 12 4.2 4.2L19 6.5" />
        )}
      </svg>
    </span>
  );
}

function Protected({ children }: { children: ReactNode }) {
  return authenticated()
    ? children
    : <Navigate to="/login" replace />;
}

function Shell({ children }: { children: ReactNode }) {
  const navigate = useNavigate();

  return (
    <div className="shell">
      <aside>
        <Logo />
        <h1>CloudBank</h1>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/accounts">Accounts</NavLink>
          <NavLink to="/transfer">Transfer</NavLink>
          <NavLink to="/beneficiaries">Beneficiaries</NavLink>
          <NavLink to="/cards">Cards</NavLink>
          <NavLink to="/loans">Loans</NavLink>
          <NavLink to="/activity">Activity</NavLink>
          <NavLink to="/profile">Profile</NavLink>
        </nav>
        <button
          onClick={() => {
            logout();
            navigate("/login", { replace: true });
          }}
        >
          Sign out
        </button>
      </aside>
      <main>{children}</main>
    </div>
  );
}

function secure(node: ReactNode) {
  return <Protected><Shell>{node}</Shell></Protected>;
}

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  if (authenticated()) {
    return <Navigate to="/dashboard" replace />;
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");

    try {
      const result = await api.login(email, password);

      if (result.tokenType !== "Bearer" || !result.accessToken) {
        throw new Error("Invalid authentication response");
      }

      saveToken(result.accessToken);
      navigate("/dashboard", { replace: true });
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to sign in",
      );
    }
  }

  return (
    <div className="auth">
      <div className="auth-card">
        <div className="auth-side">
          <div className="auth-side-top">
            <Logo />
            <span>CloudBank</span>
          </div>
          <div>
            <p className="eyebrow">Your money, simplified</p>
            <h1>Banking that moves with you.</h1>
            <p className="auth-side-copy">
              One secure place to manage your accounts, track your spending,
              and move money with confidence.
            </p>
          </div>
          <div className="auth-side-note">
            <span className="status-dot" />
            Secure access, wherever you are
          </div>
        </div>
        <form className="auth-form" onSubmit={submit}>
          <div className="auth-heading">
            <p className="eyebrow">Welcome back</p>
            <h2>Sign in to CloudBank</h2>
            <p>Enter your details to continue to your account.</p>
          </div>
          <label htmlFor="login-email">Email address</label>
          <input
            id="login-email"
            type="email"
            placeholder="you@example.com"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <div className="field-heading">
            <label htmlFor="login-password">Password</label>
            <span>Protected by encryption</span>
          </div>
          <input
            id="login-password"
            type="password"
            placeholder="Enter your password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {error && <div className="error">{error}</div>}
          <button className="auth-submit">Sign in <span>→</span></button>
          <p className="auth-switch">
            New to CloudBank? <Link to="/register">Create an account</Link>
          </p>
        </form>
      </div>
    </div>
  );
}

function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      const result = await api.register(
        email,
        password,
        firstName,
        lastName,
        phoneNumber,
        dateOfBirth,
      );
      setMessage(result.message);
      setPassword("");
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Registration failed",
      );
    }
  }

  return (
    <div className="auth">
      <div className="auth-card">
        <div className="auth-side auth-side-register">
          <div className="auth-side-top">
            <Logo />
            <span>CloudBank</span>
          </div>
          <div>
            <p className="eyebrow">Start today</p>
            <h1>Your smarter financial future starts here.</h1>
            <p className="auth-side-copy">
              Set up your secure account in seconds and get a clearer view of
              your everyday finances.
            </p>
          </div>
          <div className="auth-side-note">
            <span className="status-dot" />
            Simple, secure, built for you
          </div>
        </div>
        <form className="auth-form" onSubmit={submit}>
          <div className="auth-heading">
            <p className="eyebrow">Get started</p>
            <h2>Create your account</h2>
            <p>Join CloudBank and take control of your money.</p>
          </div>
          <div className="register-fields">
            <div>
              <label htmlFor="register-first-name">First name</label>
              <input
                id="register-first-name"
                required
                placeholder="First name"
                autoComplete="given-name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="register-last-name">Last name</label>
              <input
                id="register-last-name"
                required
                placeholder="Last name"
                autoComplete="family-name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
            </div>
          </div>
          <label htmlFor="register-email">Email address</label>
          <input
            id="register-email"
            type="email"
            required
            placeholder="you@example.com"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <label htmlFor="register-password">Create a password</label>
          <input
            id="register-password"
            type="password"
            required
            minLength={8}
            maxLength={72}
            placeholder="At least 8 characters"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <div className="register-fields">
            <div>
              <label htmlFor="register-phone">Phone number</label>
              <input
                id="register-phone"
                type="tel"
                required
                maxLength={32}
                placeholder="+1 555 000 0000"
                autoComplete="tel"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="register-dob">Date of birth</label>
              <input
                id="register-dob"
                type="date"
                required
                value={dateOfBirth}
                onChange={(e) => setDateOfBirth(e.target.value)}
              />
            </div>
          </div>
          {message && <div className="success">{message}</div>}
          {error && <div className="error">{error}</div>}
          <button className="auth-submit">Create account <span>→</span></button>
          <p className="auth-switch">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
}

function Dashboard() {
  const [profile, setProfile] =
    useState<CustomerProfileResponse | null>(null);
  const [accounts, setAccounts] =
    useState<AccountResponse[]>([]);
  const [balances, setBalances] =
    useState<Record<string, LedgerBalanceResult>>({});
  const [error, setError] = useState("");

  useEffect(() => {
    void (async () => {
      try {
        let p: CustomerProfileResponse | null = null;

        try {
          p = await api.profile();
        } catch (failure) {
          if (!(failure instanceof ApiError && failure.status === 404)) {
            throw failure;
          }
        }

        setProfile(p);

        if (!p) return;

        const list = await api.accounts();
        setAccounts(list);

        const next: Record<string, LedgerBalanceResult> = {};

        await Promise.all(
          list.map(async (account) => {
            try {
              next[account.id] = await api.balance(account.id);
            } catch {
              // Dashboard can render if one balance lookup fails.
            }
          }),
        );

        setBalances(next);
      } catch (failure) {
        setError(
          failure instanceof Error
            ? failure.message
            : "Unable to load dashboard",
        );
      }
    })();
  }, []);

  return (
    <>
      <div className="dashboard-hero">
        <div>
          <span className="eyebrow">Overview</span>
          <h2>
            {profile?.firstName
              ? `Good morning, ${profile.firstName}`
              : "Welcome to CloudBank"}
          </h2>
          <p>Here is your financial snapshot for today.</p>
        </div>
        <div className="dashboard-hero-actions">
          <div className="header-chip"><i className="status-dot" /> Secure banking</div>
          <Link className="hero-action" to="/accounts">View accounts <span>→</span></Link>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      {!profile && (
        <div className="warning">
          Complete your profile before opening an account.{" "}
          <Link to="/profile">Complete profile</Link>
        </div>
      )}

      <div className="stats">
        <article>
          <div className="stat-top"><span>Accounts</span><StatIcon type="accounts" /></div>
          <strong>{accounts.length}</strong>
          <small>Across your portfolio</small>
        </article>
        <article>
          <div className="stat-top"><span>Profile status</span><StatIcon type="profile" /></div>
          <strong className="stat-value-text">{profile?.status ?? "INCOMPLETE"}</strong>
          <small>Verification status</small>
        </article>
        <article>
          <div className="stat-top"><span>Active accounts</span><StatIcon type="active" /></div>
          <strong>
            {accounts.filter((a) => a.status === "ACTIVE").length}
          </strong>
          <small>Ready to transact</small>
        </article>
      </div>

      <section className="panel spotlight">
        <div className="spotlight-copy">
          <span className="eyebrow">Financial health</span>
          <h3>Everything is in good standing.</h3>
          <p>Your accounts and transfers are protected by CloudBank security.</p>
        </div>
        <div className="legend">
          <span><i className="dot green" /> Active accounts</span>
          <span><i className="dot blue" /> Protected transfers</span>
        </div>
      </section>

      <section className="accounts-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Your portfolio</span>
            <h3>Your accounts</h3>
          </div>
          <Link to="/accounts">Manage accounts <span>→</span></Link>
        </div>
        <div className="panel account-grid">
          {accounts.length === 0 ? (
            <div className="empty-accounts">
              <span className="empty-icon">+</span>
              <strong>No accounts yet</strong>
              <span>Open your first account to get started.</span>
              <Link to="/accounts">Open an account</Link>
            </div>
          ) : accounts.map((account) => (
            <Link
              className="account"
              key={account.id}
              to={`/accounts/${account.id}`}
            >
              <div className="account-meta">
                <span className="account-label">{account.accountType}</span>
                <strong>{account.accountNumber}</strong>
              </div>
              <span className="account-currency">{account.currency} account</span>
              <b>
                {balances[account.id]
                  ? money(
                      balances[account.id].postedBalance,
                      balances[account.id].currency,
                    )
                  : "Balance unavailable"}
              </b>
              <em className={`status-badge ${account.status.toLowerCase()}`}>
                {account.status}
              </em>
            </Link>
          ))}
        </div>
      </section>
    </>
  );
}

function Profile() {
  const empty: CustomerProfilePayload = {
    firstName: null,
    lastName: null,
    phoneNumber: null,
    dateOfBirth: null,
  };

  const [existing, setExisting] =
    useState<CustomerProfileResponse | null>(null);
  const [form, setForm] =
    useState<CustomerProfilePayload>(empty);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void api.profile()
      .then((result) => {
        setExisting(result);
        setForm({
          firstName: result.firstName,
          lastName: result.lastName,
          phoneNumber: result.phoneNumber,
          dateOfBirth: result.dateOfBirth,
        });
      })
      .catch((failure) => {
        if (!(failure instanceof ApiError && failure.status === 404)) {
          setError("Unable to load profile");
        }
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  function update(
    key: keyof CustomerProfilePayload,
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [key]: value || null,
    }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!form.firstName?.trim() || !form.lastName?.trim()) {
      setError("Please enter your first and last name.");
      return;
    }

    if (form.phoneNumber && !/^[+\d\s().-]{7,32}$/.test(form.phoneNumber)) {
      setError("Please enter a valid phone number.");
      return;
    }

    setSaving(true);
    try {
      const result = existing
        ? await api.updateProfile(form)
        : await api.createProfile(form);

      setExisting(result);
      setForm({
        firstName: result.firstName,
        lastName: result.lastName,
        phoneNumber: result.phoneNumber,
        dateOfBirth: result.dateOfBirth,
      });
      setMessage("Your profile details have been saved.");
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to save profile",
      );
    } finally {
      setSaving(false);
    }
  }

  const displayName = [form.firstName, form.lastName]
    .filter(Boolean)
    .join(" ") || "Your profile";
  const initials = [form.firstName, form.lastName]
    .filter(Boolean)
    .map((name) => name?.[0]?.toUpperCase())
    .join("") || "?";

  return (
    <>
      <div className="page-heading profile-heading">
        <div>
          <p className="eyebrow">Account settings</p>
          <h2>Customer profile</h2>
          <p>Keep your personal details up to date for a smoother banking experience.</p>
        </div>
        {existing && (
          <span className={`status-badge ${existing.status.toLowerCase()}`}>
            <span className="status-dot-inline" />
            {existing.status}
          </span>
        )}
      </div>
      <div className="profile-layout">
        <section className="panel profile-summary">
          <div className="profile-avatar">{initials}</div>
          <h3>{displayName}</h3>
          <p className="profile-summary-copy">
            {existing ? "CloudBank customer" : "Complete your profile to get started"}
          </p>
          <div className="profile-summary-divider" />
          <dl className="profile-meta">
            <div>
              <dt>Customer since</dt>
              <dd>{existing ? new Date(existing.createdAt).toLocaleDateString(undefined, {
                month: "short",
                year: "numeric",
              }) : "Not available"}</dd>
            </div>
            <div>
              <dt>Last updated</dt>
              <dd>{existing ? new Date(existing.updatedAt).toLocaleDateString() : "Not saved yet"}</dd>
            </div>
          </dl>
          <p className="profile-security-note">
            <span aria-hidden="true">✓</span>
            Your personal information is encrypted and securely stored.
          </p>
        </section>
        <form className="panel form profile-form" onSubmit={submit}>
          <div className="form-section-heading">
            <div>
              <h3>Personal information</h3>
              <p>Use your legal name and a phone number where we can reach you.</p>
            </div>
            {loading && <span className="form-loading">Loading…</span>}
          </div>
          <div className="profile-fields">
            <label>
              First name
              <input
                placeholder="e.g. Maya"
                autoComplete="given-name"
                maxLength={100}
                value={form.firstName ?? ""}
                onChange={(e) => update("firstName", e.target.value)}
                disabled={loading || saving}
                required
              />
            </label>
            <label>
              Last name
              <input
                placeholder="e.g. Johnson"
                autoComplete="family-name"
                maxLength={100}
                value={form.lastName ?? ""}
                onChange={(e) => update("lastName", e.target.value)}
                disabled={loading || saving}
                required
              />
            </label>
            <label>
              Phone number
              <input
                type="tel"
                placeholder="+1 (555) 123-4567"
                autoComplete="tel"
                maxLength={32}
                value={form.phoneNumber ?? ""}
                onChange={(e) => update("phoneNumber", e.target.value)}
                disabled={loading || saving}
              />
              <span className="field-hint">We’ll use this for important account alerts.</span>
            </label>
            <label>
              Date of birth
              <input
                type="date"
                autoComplete="bday"
                max={new Date().toISOString().split("T")[0]}
                value={form.dateOfBirth ?? ""}
                onChange={(e) => update("dateOfBirth", e.target.value)}
                disabled={loading || saving}
              />
            </label>
          </div>
          {message && <div className="success">{message}</div>}
          {error && <div className="error">{error}</div>}
          <div className="profile-form-actions">
            <span>Changes are saved securely to your customer record.</span>
            <button disabled={loading || saving}>
              {saving ? "Saving…" : existing ? "Save changes" : "Create profile"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}

function Accounts() {
  const [items, setItems] = useState<AccountResponse[]>([]);
  const [type, setType] = useState<AccountType>("CHECKING");
  const [currency, setCurrency] = useState("USD");
  const [error, setError] = useState("");

  const refresh = async () => {
    try {
      setItems(await api.accounts());
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to load accounts",
      );
    }
  };

  useEffect(() => {
    void refresh();
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");

    try {
      await api.createAccount(type, currency.toUpperCase());
      await refresh();
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to create account",
      );
    }
  }

  return (
    <>
      <div className="page-header account-page-header">
        <div>
          <span className="eyebrow">Money management</span>
          <h2>Your accounts</h2>
          <p>Manage your CloudBank accounts in one place.</p>
        </div>
        <span className="header-chip">{items.length} account{items.length === 1 ? "" : "s"}</span>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="accounts-layout">
        <section className="panel open-account-card">
          <div className="open-account-heading">
            <span className="account-create-icon">+</span>
            <div>
              <span className="eyebrow">New account</span>
              <h3>Open an account</h3>
              <p>Choose an account type and currency to get started.</p>
            </div>
          </div>
          <form className="form" onSubmit={submit}>
            <label htmlFor="account-type">Account type</label>
            <select
              id="account-type"
              value={type}
              onChange={(e) => setType(e.target.value as AccountType)}
            >
              <option value="CHECKING">Checking</option>
              <option value="SAVINGS">Savings</option>
            </select>
            <label htmlFor="account-currency">Currency</label>
            <input
              id="account-currency"
              required
              maxLength={3}
              pattern="[A-Za-z]{3}"
              placeholder="USD"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
            />
            <button>Open account <span>→</span></button>
          </form>
        </section>

        <section className="accounts-list-panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Portfolio</span>
              <h3>All accounts</h3>
            </div>
          </div>
          {items.length === 0 ? (
            <div className="panel empty-accounts">
              <span className="empty-icon">+</span>
              <strong>No accounts yet</strong>
              <span>Your new account will appear here.</span>
            </div>
          ) : (
            <div className="account-list">
              {items.map((item) => (
                <Link className="account-row" key={item.id} to={`/accounts/${item.id}`}>
                  <span className="account-row-icon">
                    {item.accountType === "SAVINGS" ? "S" : "C"}
                  </span>
                  <span className="account-row-main">
                    <strong>{item.accountType} account</strong>
                    <small>{item.accountNumber} · {item.currency}</small>
                  </span>
                  <em className={`status-badge ${item.status.toLowerCase()}`}>
                    {item.status}
                  </em>
                  <span className="account-row-arrow">→</span>
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </>
  );
}

function AccountDetail() {
  const { accountId = "" } = useParams();
  const [account, setAccount] =
    useState<AccountResponse | null>(null);
  const [balance, setBalance] =
    useState<LedgerBalanceResult | null>(null);
  const [error, setError] = useState("");

  async function reload() {
    try {
      const [a, b] = await Promise.all([
        api.account(accountId),
        api.balance(accountId),
      ]);

      setAccount(a);
      setBalance(b);
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to load account",
      );
    }
  }

  useEffect(() => {
    void reload();
  }, [accountId]);

  async function status(next: AccountStatus) {
    try {
      setAccount(await api.updateStatus(accountId, next));
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Status update failed",
      );
    }
  }

  return (
    <>
      <h2>{account?.accountNumber ?? "Account"}</h2>
      {error && <div className="error">{error}</div>}

      {account && (
        <>
          <div className="stats">
            <article>
              <span>Balance</span>
              <strong>
                {balance
                  ? money(balance.postedBalance, balance.currency)
                  : "Unavailable"}
              </strong>
            </article>
            <article>
              <span>Type</span>
              <strong>{account.accountType}</strong>
            </article>
            <article>
              <span>Status</span>
              <strong>{account.status}</strong>
            </article>
          </div>

          <section className="panel actions">
            <Link to={`/transfer?source=${account.id}`}>
              Transfer
            </Link>
            <Link to={`/accounts/${account.id}/transactions`}>
              Transactions
            </Link>
          </section>

          <section className="panel actions">
            {(
              ["ACTIVE", "FROZEN", "CLOSED"] as AccountStatus[]
            ).map((value) => (
              <button
                key={value}
                disabled={value === account.status}
                onClick={() => void status(value)}
              >
                Set {value}
              </button>
            ))}
          </section>
        </>
      )}
    </>
  );
}

function Transfer() {
  const [query] = useSearchParams();
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [source, setSource] = useState(query.get("source") ?? "");
  const [destination, setDestination] = useState("");
  const [amount, setAmount] = useState("");
  const [result, setResult] = useState<TransferResult | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    void api.accounts()
      .then((list) => {
        setAccounts(list);

        if (!source) {
          const first = list.find((a) => a.status === "ACTIVE");
          if (first) setSource(first.id);
        }
      })
      .catch(() => setError("Unable to load accounts"));
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setResult(null);

    const value = Number(amount);

    if (!Number.isFinite(value) || value <= 0) {
      setError("Amount must be greater than zero.");
      return;
    }

    try {
      setResult(await api.transfer(source, destination, value));
      setAmount("");
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Transfer failed",
      );
    }
  }

  return (
    <>
      <h2>Transfer money</h2>
      <form className="panel form" onSubmit={submit}>
        <select
          required
          value={source}
          onChange={(e) => setSource(e.target.value)}
        >
          <option value="">Source account</option>
          {accounts
            .filter((a) => a.status === "ACTIVE")
            .map((a) => (
              <option key={a.id} value={a.id}>
                {a.accountNumber} · {a.currency}
              </option>
            ))}
        </select>

        <input
          required
          placeholder="Destination account UUID"
          value={destination}
          onChange={(e) => setDestination(e.target.value)}
        />

        <input
          required
          type="number"
          min="0.01"
          step="0.01"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />

        {error && <div className="error">{error}</div>}

        {result && (
          <div className="success">
            Transfer complete: {money(result.amount, result.currency)}
          </div>
        )}

        <button>Transfer</button>
      </form>
    </>
  );
}

function Transactions() {
  const { accountId = "" } = useParams();
  const [page, setPage] = useState(0);
  const [data, setData] =
    useState<LedgerTransactionPage | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    void api.transactions(accountId, page)
      .then(setData)
      .catch((failure) =>
        setError(
          failure instanceof Error
            ? failure.message
            : "Unable to load transactions",
        ),
      );
  }, [accountId, page]);

  return (
    <>
      <h2>Transaction history</h2>
      {error && <div className="error">{error}</div>}

      <section className="panel">
        {data && (
          <>
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Reference</th>
                  <th>Amount</th>
                </tr>
              </thead>
              <tbody>
                {data.transactions.map((entry) => (
                  <tr key={entry.postingId}>
                    <td>{new Date(entry.createdAt).toLocaleString()}</td>
                    <td>{entry.entryType}</td>
                    <td>{entry.referenceType}</td>
                    <td>{money(entry.amount, entry.currency)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="actions">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Previous
              </button>

              <span>
                Page {data.page + 1} / {Math.max(data.totalPages, 1)}
              </span>

              <button
                disabled={!data.hasNext}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          </>
        )}
      </section>
    </>
  );
}

function Cards() {
  const [items, setItems] = useState<CardResponse[]>([]);
  const [name, setName] = useState("");
  const [type, setType] = useState<CardType>("DEBIT");
  const [currency, setCurrency] = useState("USD");
  const [error, setError] = useState("");

  async function refresh() {
    try {
      setItems(await api.cards());
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : "Unable to load cards");
    }
  }

  useEffect(() => { void refresh(); }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      await api.createCard(name, type, currency.toUpperCase());
      setName("");
      await refresh();
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : "Unable to create card");
    }
  }

  async function updateStatus(card: CardResponse, status: CardStatus) {
    try {
      const updated = await api.updateCardStatus(card.id, status);
      setItems((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : "Unable to update card");
    }
  }

  return (
    <>
      <div className="page-header account-page-header">
        <div><span className="eyebrow">Payment tools</span><h2>Your cards</h2><p>Manage your CloudBank debit and credit cards.</p></div>
        <span className="header-chip">{items.length} card{items.length === 1 ? "" : "s"}</span>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="accounts-layout">
        <form className="panel form" onSubmit={submit}>
          <span className="eyebrow">New card</span>
          <h3>Issue a card</h3>
          <label>Cardholder name</label>
          <input required maxLength={120} value={name} onChange={(event) => setName(event.target.value)} placeholder="Name on card" />
          <label>Card type</label>
          <select value={type} onChange={(event) => setType(event.target.value as CardType)}>
            <option value="DEBIT">Debit</option><option value="CREDIT">Credit</option>
          </select>
          <label>Currency</label>
          <input required maxLength={3} pattern="[A-Za-z]{3}" value={currency} onChange={(event) => setCurrency(event.target.value)} />
          <button>Issue card <span>→</span></button>
        </form>
        <section className="card-list">
          {items.length === 0 ? <div className="panel empty-accounts"><strong>No cards yet</strong><span>Issue your first card to get started.</span></div> :
            items.map((card) => (
              <article className="payment-card" key={card.id}>
                <div className="payment-card-top"><span>{card.type} CARD</span><strong>CloudBank</strong></div>
                <div className="payment-card-number">{card.maskedNumber}</div>
                <div className="payment-card-bottom"><span>{card.cardholderName}</span><span>{String(card.expirationMonth).padStart(2, "0")}/{card.expirationYear}</span></div>
                <div className="actions"><em className={`status-badge ${card.status.toLowerCase()}`}>{card.status}</em>
                  {card.status === "ACTIVE" && <button onClick={() => void updateStatus(card, "BLOCKED")}>Block card</button>}
                  {card.status === "BLOCKED" && <button onClick={() => void updateStatus(card, "ACTIVE")}>Unblock card</button>}
                </div>
              </article>
            ))}
        </section>
      </div>
    </>
  );
}

function Beneficiaries() {
  const empty: Omit<BeneficiaryResponse, "id" | "authUserId" | "createdAt" | "updatedAt"> = {
    nickname: "", accountHolderName: "", accountNumber: "", bankName: "", currency: "USD",
  };
  const [items, setItems] = useState<BeneficiaryResponse[]>([]);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState<string | null>(null);
  const [error, setError] = useState("");
  const update = (key: keyof typeof empty, value: string) => setForm((current) => ({ ...current, [key]: value }));

  async function refresh() {
    try { setItems(await api.beneficiaries()); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to load beneficiaries"); }
  }
  useEffect(() => { void refresh(); }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      if (editing) await api.updateBeneficiary(editing, form);
      else await api.createBeneficiary(form);
      setForm(empty); setEditing(null); await refresh();
    } catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to save beneficiary"); }
  }

  async function remove(id: string) {
    if (!window.confirm("Remove this beneficiary?")) return;
    try { await api.deleteBeneficiary(id); await refresh(); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to remove beneficiary"); }
  }

  return (
    <>
      <div className="page-header account-page-header"><div><span className="eyebrow">Payees</span><h2>Beneficiaries</h2><p>Keep trusted recipients ready for transfers.</p></div></div>
      {error && <div className="error">{error}</div>}
      <div className="accounts-layout">
        <form className="panel form" onSubmit={submit}><span className="eyebrow">{editing ? "Edit payee" : "New payee"}</span><h3>{editing ? "Update beneficiary" : "Add beneficiary"}</h3>
          <input required placeholder="Nickname" value={form.nickname} onChange={(event) => update("nickname", event.target.value)} />
          <input required placeholder="Account holder name" value={form.accountHolderName} onChange={(event) => update("accountHolderName", event.target.value)} />
          <input required placeholder="Account number" value={form.accountNumber} onChange={(event) => update("accountNumber", event.target.value)} />
          <input placeholder="Bank name" value={form.bankName ?? ""} onChange={(event) => update("bankName", event.target.value)} />
          <input required maxLength={3} pattern="[A-Za-z]{3}" placeholder="Currency" value={form.currency} onChange={(event) => update("currency", event.target.value.toUpperCase())} />
          <button>{editing ? "Save changes" : "Add beneficiary"}</button>
          {editing && <button type="button" className="secondary-button" onClick={() => { setEditing(null); setForm(empty); }}>Cancel</button>}
        </form>
        <section className="beneficiary-list">
          {items.length === 0 ? <div className="panel empty-accounts"><strong>No beneficiaries yet</strong><span>Add a recipient to make transfers easier.</span></div> :
            items.map((item) => <article className="panel beneficiary-row" key={item.id}><div><strong>{item.nickname}</strong><small>{item.accountHolderName} · {item.accountNumber}</small><small>{item.bankName || "External bank"} · {item.currency}</small></div><div className="actions"><button onClick={() => { setEditing(item.id); setForm({ nickname: item.nickname, accountHolderName: item.accountHolderName, accountNumber: item.accountNumber, bankName: item.bankName ?? "", currency: item.currency }); }}>Edit</button><button className="danger-button" onClick={() => void remove(item.id)}>Remove</button></div></article>)}
        </section>
      </div>
    </>
  );
}

function Loans() {
  const [items, setItems] = useState<LoanResponse[]>([]);
  const [principal, setPrincipal] = useState("");
  const [rate, setRate] = useState("8.5");
  const [term, setTerm] = useState("24");
  const [currency, setCurrency] = useState("USD");
  const [error, setError] = useState("");
  async function refresh() {
    try { setItems(await api.loans()); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to load loans"); }
  }
  useEffect(() => { void refresh(); }, []);
  async function submit(event: FormEvent) {
    event.preventDefault();
    try { await api.createLoan(Number(principal), Number(rate), Number(term), currency.toUpperCase()); setPrincipal(""); await refresh(); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to apply for loan"); }
  }
  async function updateStatus(loan: LoanResponse, status: LoanStatus) {
    try { const updated = await api.updateLoanStatus(loan.id, status); setItems((current) => current.map((item) => item.id === updated.id ? updated : item)); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to update loan"); }
  }
  return (
    <>
      <div className="page-header account-page-header"><div><span className="eyebrow">Borrowing</span><h2>Loans</h2><p>Review applications and keep your borrowing goals on track.</p></div></div>
      {error && <div className="error">{error}</div>}
      <div className="accounts-layout">
        <form className="panel form" onSubmit={submit}><span className="eyebrow">New application</span><h3>Apply for a loan</h3><label>Principal amount</label><input required type="number" min="0.01" step="0.01" value={principal} onChange={(event) => setPrincipal(event.target.value)} placeholder="25000" /><label>Annual interest rate (%)</label><input required type="number" min="0" step="0.01" value={rate} onChange={(event) => setRate(event.target.value)} /><label>Term (months)</label><input required type="number" min="1" value={term} onChange={(event) => setTerm(event.target.value)} /><label>Currency</label><input required maxLength={3} pattern="[A-Za-z]{3}" value={currency} onChange={(event) => setCurrency(event.target.value)} /><button>Submit application <span>→</span></button></form>
        <section className="loan-list">{items.length === 0 ? <div className="panel empty-accounts"><strong>No loan applications</strong><span>Your applications will appear here.</span></div> :         items.map((loan) => <article className="panel loan-row" key={loan.id}><div><span className="eyebrow">{loan.currency} loan</span><h3>{money(loan.principalAmount, loan.currency)}</h3><small>{loan.annualInterestRate}% APR · {loan.termMonths} months</small></div><div className="actions"><em className={`status-badge ${loan.status.toLowerCase()}`}>{loan.status}</em>{loan.status === "APPLICATION" && <button onClick={() => void updateStatus(loan, "REJECTED")}>Withdraw</button>}</div></article>)}</section>
      </div>
    </>
  );
}

function Activity() {
  const [items, setItems] = useState<TransactionResponse[]>([]);
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [type, setType] = useState<TransactionType>("DEBIT");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  async function refresh() {
    try { setItems(await api.customerTransactions()); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to load activity"); }
  }
  useEffect(() => { void refresh(); void api.accounts().then(setAccounts).catch(() => undefined); }, []);
  async function submit(event: FormEvent) {
    event.preventDefault();
    const account = accounts.find((item) => item.id === accountId);
    if (!account) { setError("Choose an account."); return; }
    try { await api.createTransaction(accountId, Number(amount), account.currency, type, description); setAmount(""); setDescription(""); await refresh(); }
    catch (failure) { setError(failure instanceof Error ? failure.message : "Unable to record transaction"); }
  }
  return (
    <>
      <div className="page-header account-page-header"><div><span className="eyebrow">Money movement</span><h2>Activity</h2><p>Review your transaction timeline.</p></div></div>
      {error && <div className="error">{error}</div>}
      <form className="panel form" onSubmit={submit}><span className="eyebrow">Manual entry</span><h3>Record transaction</h3><p>Manual records do not change your account balance. Use Transfers to move money.</p><select required value={accountId} onChange={(event) => setAccountId(event.target.value)}><option value="">Choose account</option>{accounts.map((account) => <option value={account.id} key={account.id}>{account.accountNumber} · {account.currency}</option>)}</select><select value={type} onChange={(event) => setType(event.target.value as TransactionType)}><option value="DEBIT">Debit</option><option value="CREDIT">Credit</option><option value="TRANSFER">Transfer</option></select><input required type="number" min="0.01" step="0.01" placeholder="Amount" value={amount} onChange={(event) => setAmount(event.target.value)} /><input required placeholder="Description" value={description} onChange={(event) => setDescription(event.target.value)} /><button>Record transaction</button></form>
      <section className="panel"><table><thead><tr><th>Date</th><th>Description</th><th>Type</th><th>Status</th><th>Amount</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{new Date(item.createdAt).toLocaleDateString()}</td><td>{item.description || "Transaction"}</td><td>{item.type}</td><td><em className={`status-badge ${item.status.toLowerCase()}`}>{item.status}</em></td><td>{money(item.amount, item.currency)}</td></tr>)}</tbody></table>{items.length === 0 && <div className="empty-accounts">No transaction activity yet.</div>}</section>
    </>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={secure(<Dashboard />)} />
      <Route path="/profile" element={secure(<Profile />)} />
      <Route path="/accounts" element={secure(<Accounts />)} />
      <Route
        path="/accounts/:accountId"
        element={secure(<AccountDetail />)}
      />
      <Route path="/transfer" element={secure(<Transfer />)} />
      <Route path="/cards" element={secure(<Cards />)} />
      <Route path="/beneficiaries" element={secure(<Beneficiaries />)} />
      <Route path="/loans" element={secure(<Loans />)} />
      <Route path="/activity" element={secure(<Activity />)} />
      <Route
        path="/accounts/:accountId/transactions"
        element={secure(<Transactions />)}
      />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
