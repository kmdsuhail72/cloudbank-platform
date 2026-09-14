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
        <div className="logo">CB</div>
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
      <form className="card" onSubmit={submit}>
        <div className="logo">CB</div>
        <h1>CloudBank</h1>
        <p>Sign in to your digital bank.</p>
        <input
          type="email"
          placeholder="Email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          type="password"
          placeholder="Password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <div className="error">{error}</div>}
        <button>Sign in</button>
        <Link to="/register">Create login</Link>
      </form>
    </div>
  );
}

function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      const result = await api.register(email, password);
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
      <form className="card" onSubmit={submit}>
        <div className="logo">CB</div>
        <h1>Create login</h1>
        <input
          type="email"
          required
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          type="password"
          required
          minLength={8}
          maxLength={72}
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {message && <div className="success">{message}</div>}
        {error && <div className="error">{error}</div>}
        <button>Register</button>
        <Link to="/login">Back to sign in</Link>
      </form>
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
      <h2>
        {profile?.firstName
          ? `Welcome, ${profile.firstName}`
          : "CloudBank Dashboard"}
      </h2>

      {error && <div className="error">{error}</div>}

      {!profile && (
        <div className="warning">
          Complete your profile before opening an account.{" "}
          <Link to="/profile">Complete profile</Link>
        </div>
      )}

      <div className="stats">
        <article>
          <span>Accounts</span>
          <strong>{accounts.length}</strong>
        </article>
        <article>
          <span>Profile</span>
          <strong>{profile?.status ?? "INCOMPLETE"}</strong>
        </article>
        <article>
          <span>Active</span>
          <strong>
            {accounts.filter((a) => a.status === "ACTIVE").length}
          </strong>
        </article>
      </div>

      <section className="panel grid">
        {accounts.map((account) => (
          <Link
            className="account"
            key={account.id}
            to={`/accounts/${account.id}`}
          >
            <strong>{account.accountNumber}</strong>
            <span>{account.accountType} · {account.currency}</span>
            <b>
              {balances[account.id]
                ? money(
                    balances[account.id].postedBalance,
                    balances[account.id].currency,
                  )
                : "Balance unavailable"}
            </b>
          </Link>
        ))}
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
      <h2>Accounts</h2>

      <form className="panel form" onSubmit={submit}>
        <select
          value={type}
          onChange={(e) => setType(e.target.value as AccountType)}
        >
          <option value="CHECKING">Checking</option>
          <option value="SAVINGS">Savings</option>
        </select>

        <input
          required
          maxLength={3}
          pattern="[A-Za-z]{3}"
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
        />

        <button>Open account</button>
      </form>

      {error && <div className="error">{error}</div>}

      <section className="panel">
        <table>
          <thead>
            <tr>
              <th>Account</th>
              <th>Type</th>
              <th>Currency</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.accountNumber}</td>
                <td>{item.accountType}</td>
                <td>{item.currency}</td>
                <td>{item.status}</td>
                <td>
                  <Link to={`/accounts/${item.id}`}>View</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
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
