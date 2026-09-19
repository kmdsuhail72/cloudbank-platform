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
  CustomerProfilePayload,
  CustomerProfileResponse,
  LedgerBalanceResult,
  LedgerTransactionPage,
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

    try {
      const result = existing
        ? await api.updateProfile(form)
        : await api.createProfile(form);

      setExisting(result);
      setMessage("Profile saved.");
    } catch (failure) {
      setError(
        failure instanceof Error
          ? failure.message
          : "Unable to save profile",
      );
    }
  }

  return (
    <>
      <h2>Customer profile</h2>
      <form className="panel form" onSubmit={submit}>
        <input
          placeholder="First name"
          maxLength={100}
          value={form.firstName ?? ""}
          onChange={(e) => update("firstName", e.target.value)}
        />
        <input
          placeholder="Last name"
          maxLength={100}
          value={form.lastName ?? ""}
          onChange={(e) => update("lastName", e.target.value)}
        />
        <input
          placeholder="Phone"
          maxLength={32}
          value={form.phoneNumber ?? ""}
          onChange={(e) => update("phoneNumber", e.target.value)}
        />
        <input
          type="date"
          value={form.dateOfBirth ?? ""}
          onChange={(e) => update("dateOfBirth", e.target.value)}
        />
        {message && <div className="success">{message}</div>}
        {error && <div className="error">{error}</div>}
        <button>{existing ? "Update profile" : "Create profile"}</button>
      </form>
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
      <Route
        path="/accounts/:accountId/transactions"
        element={secure(<Transactions />)}
      />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
