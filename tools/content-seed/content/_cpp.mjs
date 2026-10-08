// C++17 starter code and reference solution for every coding task, keyed by task code.
// Python and Java live with the task itself (das1110.mjs …); C++ was added later as the third
// teaching language (product spec S-05.2), so it is kept here and merged by task code.
//
// Every solution is checked against the task's tests by verify-solutions.mjs (g++ -std=c++17 -O2,
// the same image and flags as the sandbox). Text is String.raw so "\n" stays a C++ escape.
const r = String.raw

export default {
  // ============================================================================ DAS1110, semester 1
  'DAS-S1-A1': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string name;
    getline(cin, name);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string name;
    getline(cin, name);
    size_t first = name.find_first_not_of(" \t\r");
    size_t last = name.find_last_not_of(" \t\r");
    name = first == string::npos ? "" : name.substr(first, last - first + 1);
    cout << "Salom, " << name << "!" << endl;
    return 0;
}
`,
  },
  'DAS-S1-A2': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    cout << a + b << "\n";
    cout << a - b << "\n";
    cout << a * b << "\n";
    cout << a / b << "\n";
    cout << a % b << "\n";
    return 0;
}
`,
  },
  'DAS-S1-A3': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int y;
    cin >> y;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int y;
    cin >> y;
    bool leap = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0;
    cout << (leap ? "Kabisa" : "Kabisa emas") << endl;
    return 0;
}
`,
  },
  'DAS-S1-A4': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long a, b, c;
    cin >> a >> b >> c;
    long long d = b * b - 4 * a * c;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long a, b, c;
    cin >> a >> b >> c;
    long long d = b * b - 4 * a * c;
    if (d > 0) cout << 2 << endl;
    else if (d == 0) cout << 1 << endl;
    else cout << 0 << endl;
    return 0;
}
`,
  },
  'DAS-S1-A5': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    switch (n) {
        case 1: cout << "Dushanba" << endl; break;
        // ...
        default: cout << "Xato" << endl;
    }
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    switch (n) {
        case 1: cout << "Dushanba" << endl; break;
        case 2: cout << "Seshanba" << endl; break;
        case 3: cout << "Chorshanba" << endl; break;
        case 4: cout << "Payshanba" << endl; break;
        case 5: cout << "Juma" << endl; break;
        case 6: cout << "Shanba" << endl; break;
        case 7: cout << "Yakshanba" << endl; break;
        default: cout << "Xato" << endl;
    }
    return 0;
}
`,
  },
  'DAS-S1-A6': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    long long f = 1;
    // ...
    cout << f << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    long long f = 1;
    for (int i = 2; i <= n; i++) f *= i;
    cout << f << endl;
    return 0;
}
`,
  },
  'DAS-S1-A7': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long d = 2;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    bool prime = n >= 2;
    long long d = 2;
    while (d * d <= n && prime) {
        if (n % d == 0) prime = false;
        d++;
    }
    cout << (prime ? "Tub" : "Tub emas") << endl;
    return 0;
}
`,
  },
  'DAS-S1-A8': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    for (int i = 1; i <= n; i++) {
        for (int j = 1; j <= n; j++) {
            if (j > 1) cout << ' ';
            cout << i * j;
        }
        cout << "\n";
    }
    return 0;
}
`,
  },
  'DEV-TASK-SUM': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    long long sum = 0;
    for (int i = 0; i < n; i++) {
        long long x;
        cin >> x;
        sum += x;
    }
    cout << sum << endl;
    return 0;
}
`,
  },
  'DAS-S1-A9B': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    long long x;
    cin >> x;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    long long x;
    cin >> x;
    int ans = -1;
    for (int i = 0; i < n; i++) {
        if (a[i] == x) {
            ans = i + 1;
            break;
        }
    }
    cout << ans << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT1': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long x;
    cin >> x;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long x;
    cin >> x;
    long long y;
    if (x < 0) y = x * x;
    else if (x <= 10) y = 2 * x + 1;
    else y = x - 10;
    cout << y << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT2': {
    starter: r`#include <iomanip>
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    double s = 0;
    // ...
    cout << fixed << setprecision(4) << s << endl;
    return 0;
}
`,
    solution: r`#include <iomanip>
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    double s = 0;
    for (int i = 1; i <= n; i++) s += 1.0 / i;
    cout << fixed << setprecision(4) << s << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT3': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    for (int i = n - 1; i >= 0; i--) {
        cout << a[i];
        if (i > 0) cout << ' ';
    }
    cout << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT4': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<vector<long long>> m(n, vector<long long>(n));
    for (auto &row : m)
        for (auto &v : row) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<vector<long long>> m(n, vector<long long>(n));
    for (auto &row : m)
        for (auto &v : row) cin >> v;
    long long mainSum = 0, sideSum = 0;
    for (int i = 0; i < n; i++) {
        mainSum += m[i][i];
        sideSum += m[i][n - 1 - i];
    }
    cout << mainSum << " " << sideSum << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT5': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n), b(n);
    for (auto &v : a) cin >> v;
    for (auto &v : b) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n), b(n);
    for (auto &v : a) cin >> v;
    for (auto &v : b) cin >> v;
    long long s = 0;
    for (int i = 0; i < n; i++) s += a[i] * b[i];
    cout << s << endl;
    return 0;
}
`,
  },
  'DAS-S1-MT6': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    long long k;
    cin >> n >> k;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    long long k;
    cin >> n >> k;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    long long count = 0;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (a[i] + a[j] == k) count++;
    cout << count << endl;
    return 0;
}
`,
  },

  // ============================================================================ DAS1110, semester 2
  'DAS-S2-A1': {
    starter: r`#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> a(n, vector<long long>(m));
    for (auto &row : a)
        for (auto &v : row) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < n; i++) {
        vector<long long> row(m);
        for (auto &v : row) cin >> v;
        sort(row.begin(), row.end());
        for (int j = 0; j < m; j++) {
            if (j > 0) cout << ' ';
            cout << row[j];
        }
        cout << "\n";
    }
    return 0;
}
`,
  },
  'DAS-S2-A2': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    int q;
    cin >> q;
    while (q--) {
        long long x;
        cin >> x;
        int lo = 0, hi = n - 1, ans = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == x) {
                ans = mid + 1;
                break;
            }
            if (a[mid] < x) lo = mid + 1;
            else hi = mid - 1;
        }
        cout << ans << "\n";
    }
    return 0;
}
`,
  },
  'DAS-S2-A3': {
    starter: r`#include <iostream>
using namespace std;

long long gcd(long long a, long long b) {
    // ...
    return 0;
}

long long lcm(long long a, long long b) {
    // ...
    return 0;
}

int main() {
    long long a, b;
    cin >> a >> b;
    cout << gcd(a, b) << " " << lcm(a, b) << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

long long gcd(long long a, long long b) {
    while (b != 0) {
        long long t = a % b;
        a = b;
        b = t;
    }
    return a;
}

long long lcm(long long a, long long b) {
    return a / gcd(a, b) * b;
}

int main() {
    long long a, b;
    cin >> a >> b;
    cout << gcd(a, b) << " " << lcm(a, b) << endl;
    return 0;
}
`,
  },
  'DAS-S2-A4': {
    starter: r`#include <iostream>
using namespace std;

long long power(long long a, long long n, long long m) {
    // ...
    return 0;
}

int main() {
    long long a, n, m;
    cin >> a >> n >> m;
    cout << power(a, n, m) << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

long long power(long long a, long long n, long long m) {
    if (n == 0) return 1 % m;
    long long half = power(a, n / 2, m);
    long long result = half * half % m;
    if (n % 2 == 1) result = result * (a % m) % m;
    return result;
}

int main() {
    long long a, n, m;
    cin >> a >> n >> m;
    cout << power(a, n, m) << endl;
    return 0;
}
`,
  },
  'DAS-S2-A5': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    long long total = 0;
    for (auto &v : a) {
        cin >> v;
        total += v;
    }
    bool any = false;
    for (long long x : a) {
        if (x * n > total) {
            if (any) cout << ' ';
            cout << x;
            any = true;
        }
    }
    if (!any) cout << "-";
    cout << endl;
    return 0;
}
`,
  },
  'DAS-S2-A6': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    for (size_t i = 0; i < s.size(); i++) {
        if (i > 0) cout << ' ';
        cout << (int) (unsigned char) s[i];
    }
    cout << endl;
    return 0;
}
`,
  },
  'DAS-S2-A7': {
    starter: r`#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    // ...
    return 0;
}
`,
    solution: r`#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    string clean;
    for (unsigned char ch : s)
        if (isalnum(ch)) clean += (char) tolower(ch);
    string reversed(clean.rbegin(), clean.rend());
    cout << (clean == reversed ? "Palindrom" : "Palindrom emas") << endl;
    return 0;
}
`,
  },
  'DAS-S2-A8': {
    starter: r`#include <iostream>
#include <sstream>
#include <string>
using namespace std;

long long length(const string &line) {
    long long count = 0;
    for (unsigned char ch : line)
        if ((ch & 0xC0) != 0x80) count++;
    return count;
}

int main() {
    string line;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <sstream>
#include <string>
using namespace std;

// Satrdagi belgilar soni. UTF-8 da bitta harf bir necha bayt boʻlishi mumkin (masalan, ʻ),
// shuning uchun davom baytlari (10xxxxxx) sanalmaydi.
long long length(const string &line) {
    long long count = 0;
    for (unsigned char ch : line)
        if ((ch & 0xC0) != 0x80) count++;
    return count;
}

int main() {
    string line;
    long long lines = 0, words = 0, chars = 0;
    while (getline(cin, line)) {
        lines++;
        chars += length(line);
        istringstream in(line);
        string word;
        while (in >> word) words++;
    }
    cout << lines << " " << words << " " << chars << endl;
    return 0;
}
`,
  },
  'DAS-S2-MT1': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int findMax(const vector<long long> &a) {
    // ...
    return 0;
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    int i = findMax(a);
    cout << a[i] << " " << i + 1 << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int findMax(const vector<long long> &a) {
    int best = 0;
    for (int i = 1; i < (int) a.size(); i++)
        if (a[i] > a[best]) best = i;
    return best;
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    int i = findMax(a);
    cout << a[i] << " " << i + 1 << endl;
    return 0;
}
`,
  },
  'DAS-S2-MT2': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

typedef vector<vector<long long>> Matrix;

Matrix multiply(const Matrix &a, const Matrix &b) {
    // ...
    return Matrix();
}

int main() {
    int n, m, p;
    cin >> n >> m >> p;
    Matrix a(n, vector<long long>(m)), b(m, vector<long long>(p));
    for (auto &row : a)
        for (auto &v : row) cin >> v;
    for (auto &row : b)
        for (auto &v : row) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

typedef vector<vector<long long>> Matrix;

Matrix multiply(const Matrix &a, const Matrix &b) {
    size_t n = a.size(), m = b.size(), p = b[0].size();
    Matrix c(n, vector<long long>(p, 0));
    for (size_t i = 0; i < n; i++)
        for (size_t j = 0; j < p; j++)
            for (size_t k = 0; k < m; k++) c[i][j] += a[i][k] * b[k][j];
    return c;
}

int main() {
    int n, m, p;
    cin >> n >> m >> p;
    Matrix a(n, vector<long long>(m)), b(m, vector<long long>(p));
    for (auto &row : a)
        for (auto &v : row) cin >> v;
    for (auto &row : b)
        for (auto &v : row) cin >> v;
    for (const auto &row : multiply(a, b)) {
        for (size_t j = 0; j < row.size(); j++) {
            if (j > 0) cout << ' ';
            cout << row[j];
        }
        cout << "\n";
    }
    return 0;
}
`,
  },
  'DAS-S2-MT3': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    cin >> m;
    vector<long long> b(m);
    for (auto &v : b) cin >> v;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    cin >> m;
    vector<long long> b(m);
    for (auto &v : b) cin >> v;
    int i = 0, j = 0;
    bool first = true;
    while (i < n || j < m) {
        long long next = (j >= m || (i < n && a[i] <= b[j])) ? a[i++] : b[j++];
        if (!first) cout << ' ';
        cout << next;
        first = false;
    }
    cout << endl;
    return 0;
}
`,
  },
  'DAS-S2-MT4': {
    starter: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

void hanoi(int n, int src, int dst, int via, vector<pair<int, int>> &moves) {
    // ...
}

int main() {
    int n;
    cin >> n;
    vector<pair<int, int>> moves;
    hanoi(n, 1, 3, 2, moves);
    cout << moves.size() << "\n";
    for (const auto &mv : moves) cout << mv.first << " " << mv.second << "\n";
    return 0;
}
`,
    solution: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

void hanoi(int n, int src, int dst, int via, vector<pair<int, int>> &moves) {
    if (n == 0) return;
    hanoi(n - 1, src, via, dst, moves);
    moves.push_back({src, dst});
    hanoi(n - 1, via, dst, src, moves);
}

int main() {
    int n;
    cin >> n;
    vector<pair<int, int>> moves;
    hanoi(n, 1, 3, 2, moves);
    cout << moves.size() << "\n";
    for (const auto &mv : moves) cout << mv.first << " " << mv.second << "\n";
    return 0;
}
`,
  },
  'DAS-S2-MT5': {
    starter: r`#include <iostream>
using namespace std;

long long ack(long long m, long long n) {
    // ...
    return 0;
}

int main() {
    long long m, n;
    cin >> m >> n;
    cout << ack(m, n) << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

long long ack(long long m, long long n) {
    if (m == 0) return n + 1;
    if (n == 0) return ack(m - 1, 1);
    return ack(m - 1, ack(m, n - 1));
}

int main() {
    long long m, n;
    cin >> m >> n;
    cout << ack(m, n) << endl;
    return 0;
}
`,
  },
  'DAS-S2-MT6': {
    starter: r`#include <cctype>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    istringstream in(s);
    string word;
    // ...
    return 0;
}
`,
    solution: r`#include <cctype>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);
    istringstream in(s);
    string word;
    bool first = true;
    while (in >> word) {
        word[0] = (char) toupper((unsigned char) word[0]);
        for (size_t i = 1; i < word.size(); i++) word[i] = (char) tolower((unsigned char) word[i]);
        if (!first) cout << ' ';
        cout << word;
        first = false;
    }
    cout << endl;
    return 0;
}
`,
  },
  'DAS-S2-MT7': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

class BankAccount {
private:
    long long balance_ = 0;

public:
    // ...
};

int main() {
    int n;
    cin >> n;
    BankAccount acc;
    for (int i = 0; i < n; i++) {
        string command;
        cin >> command;
        // ...
    }
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

class BankAccount {
private:
    long long balance_ = 0;

public:
    void deposit(long long amount) { balance_ += amount; }

    bool withdraw(long long amount) {
        if (amount > balance_) return false;
        balance_ -= amount;
        return true;
    }

    long long balance() const { return balance_; }
};

int main() {
    int n;
    cin >> n;
    BankAccount acc;
    for (int i = 0; i < n; i++) {
        string command;
        cin >> command;
        if (command == "deposit") {
            long long amount;
            cin >> amount;
            acc.deposit(amount);
        } else if (command == "withdraw") {
            long long amount;
            cin >> amount;
            if (!acc.withdraw(amount)) cout << "Mablag yetarli emas\n";
        } else {
            cout << acc.balance() << "\n";
        }
    }
    return 0;
}
`,
  },

  // ============================================================================ MTA1204
  'MTA-A1': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    int steps = 0;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    int steps = 0;
    while (b != 0) {
        long long t = a % b;
        a = b;
        b = t;
        steps++;
    }
    cout << a << " " << steps << endl;
    return 0;
}
`,
  },
  'MTA-A2': {
    starter: r`#include <iostream>
using namespace std;

long long memo[91];

long long fib(int n) {
    // ...
    return 0;
}

int main() {
    int n;
    cin >> n;
    cout << fib(n) << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

long long memo[91];

long long fib(int n) {
    if (n < 2) return n;
    if (memo[n] != 0) return memo[n];
    return memo[n] = fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    cout << fib(n) << endl;
    return 0;
}
`,
  },
  'MTA-A3': {
    starter: r`#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <vector>
using namespace std;

int lowerBound(const vector<long long> &a, long long x) {
    int lo = 0, hi = (int) a.size();
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] < x) lo = mid + 1;
        else hi = mid;
    }
    return lo;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    int q;
    cin >> q;
    while (q--) {
        long long x;
        cin >> x;
        cout << lowerBound(a, x + 1) - lowerBound(a, x) << "\n";
    }
    return 0;
}
`,
  },
  'MTA-A4': {
    starter: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

void siftDown(vector<long long> &a, int i, int n) {
    // ...
}

void heapSort(vector<long long> &a) {
    // ...
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    heapSort(a);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

void siftDown(vector<long long> &a, int i, int n) {
    while (true) {
        int big = i, l = 2 * i + 1, r = 2 * i + 2;
        if (l < n && a[l] > a[big]) big = l;
        if (r < n && a[r] > a[big]) big = r;
        if (big == i) return;
        swap(a[i], a[big]);
        i = big;
    }
}

void heapSort(vector<long long> &a) {
    int n = (int) a.size();
    for (int i = n / 2 - 1; i >= 0; i--) siftDown(a, i, n);
    for (int end = n - 1; end > 0; end--) {
        swap(a[0], a[end]);
        siftDown(a, 0, end);
    }
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    heapSort(a);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
`,
  },
  'MTA-A5': {
    starter: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    long long swaps = 0;
    // ...
    cout << swaps << endl;
    return 0;
}
`,
    solution: r`#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto &v : a) cin >> v;
    long long swaps = 0;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n - 1 - i; j++)
            if (a[j] > a[j + 1]) {
                swap(a[j], a[j + 1]);
                swaps++;
            }
    cout << swaps << endl;
    return 0;
}
`,
  },
  'MTA-A6': {
    starter: r`#include <iostream>
#include <stack>
#include <string>
using namespace std;

int main() {
    string s;
    cin >> s;
    stack<char> st;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <stack>
#include <string>
using namespace std;

int main() {
    string s;
    cin >> s;
    stack<char> st;
    bool ok = true;
    for (char ch : s) {
        if (ch == '(' || ch == '[' || ch == '{') {
            st.push(ch);
        } else {
            char open = ch == ')' ? '(' : ch == ']' ? '[' : '{';
            if (st.empty() || st.top() != open) {
                ok = false;
                break;
            }
            st.pop();
        }
    }
    cout << (ok && st.empty() ? "Muvozanatli" : "Muvozanatsiz") << endl;
    return 0;
}
`,
  },
  'MTA-A7': {
    starter: r`#include <climits>
#include <functional>
#include <iostream>
#include <queue>
#include <utility>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    // ...
    return 0;
}
`,
    solution: r`#include <climits>
#include <functional>
#include <iostream>
#include <queue>
#include <utility>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n, m;
    cin >> n >> m;
    vector<vector<pair<int, long long>>> adj(n + 1);
    for (int i = 0; i < m; i++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        adj[u].push_back({v, w});
        adj[v].push_back({u, w});
    }
    int s;
    cin >> s;
    vector<long long> dist(n + 1, LLONG_MAX);
    dist[s] = 0;
    priority_queue<pair<long long, int>, vector<pair<long long, int>>, greater<pair<long long, int>>> pq;
    pq.push({0, s});
    while (!pq.empty()) {
        pair<long long, int> top = pq.top();
        pq.pop();
        int u = top.second;
        if (top.first > dist[u]) continue;
        for (const auto &edge : adj[u]) {
            int v = edge.first;
            if (top.first + edge.second < dist[v]) {
                dist[v] = top.first + edge.second;
                pq.push({dist[v], v});
            }
        }
    }
    for (int i = 1; i <= n; i++) {
        if (i > 1) cout << ' ';
        if (dist[i] == LLONG_MAX) cout << -1;
        else cout << dist[i];
    }
    cout << endl;
    return 0;
}
`,
  },
  'MTA-A8': {
    starter: r`#include <iostream>
#include <string>
#include <vector>
using namespace std;

int n;
vector<long long> t;

void add(int i, long long x) {
    // ...
}

long long prefix(int i) {
    // ...
    return 0;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
#include <vector>
using namespace std;

int n;
vector<long long> t;

void add(int i, long long x) {
    for (; i <= n; i += i & -i) t[i] += x;
}

long long prefix(int i) {
    long long s = 0;
    for (; i > 0; i -= i & -i) s += t[i];
    return s;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    cin >> n;
    t.assign(n + 1, 0);
    for (int i = 1; i <= n; i++) {
        long long x;
        cin >> x;
        add(i, x);
    }
    int q;
    cin >> q;
    while (q--) {
        string op;
        int a;
        long long b;
        cin >> op >> a >> b;
        if (op == "add") add(a, b);
        else cout << prefix((int) b) - prefix(a - 1) << "\n";
    }
    return 0;
}
`,
  },

  // ============================================================================ KIA1406
  'KIA-A2': {
    starter: r`#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

string level(int s) {
    if (s <= 4) return "Past";
    if (s <= 9) return "O'rta";
    if (s <= 16) return "Yuqori";
    return "Kritik";
}

int main() {
    int n;
    cin >> n;
    vector<string> names(n);
    vector<int> scores(n), order(n);
    for (int k = 0; k < n; k++) {
        int probability, impact;
        cin >> names[k] >> probability >> impact;
        scores[k] = probability * impact;
        order[k] = k;
    }
    stable_sort(order.begin(), order.end(), [&](int a, int b) { return scores[a] > scores[b]; });
    for (int k : order) cout << names[k] << " " << scores[k] << " " << level(scores[k]) << "\n";
    return 0;
}
`,
  },
  'KIA-A5C': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

// ...
int floorMod(int x, int m) {
    return ((x % m) + m) % m;
}

int main() {
    string line, text;
    getline(cin, line);
    int k = stoi(line);
    getline(cin, text);
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

// Manfiy sonlar uchun ham 0..25 oraligʻidagi qoldiq
int floorMod(int x, int m) {
    return ((x % m) + m) % m;
}

int main() {
    string line, text;
    getline(cin, line);
    int k = stoi(line);
    getline(cin, text);
    for (char &ch : text) {
        if (ch >= 'a' && ch <= 'z') ch = (char) ('a' + floorMod(ch - 'a' + k, 26));
        else if (ch >= 'A' && ch <= 'Z') ch = (char) ('A' + floorMod(ch - 'A' + k, 26));
    }
    cout << text << endl;
    return 0;
}
`,
  },
  'KIA-A5V': {
    starter: r`#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int floorMod(int x, int m) {
    return ((x % m) + m) % m;
}

string trim(const string &s) {
    size_t first = s.find_first_not_of(" \t\r");
    size_t last = s.find_last_not_of(" \t\r");
    return first == string::npos ? "" : s.substr(first, last - first + 1);
}

int main() {
    string mode, key, text;
    getline(cin, mode);
    getline(cin, key);
    getline(cin, text);
    mode = trim(mode);
    key = trim(key);
    for (char &ch : key) ch = (char) toupper((unsigned char) ch);
    // ...
    return 0;
}
`,
    solution: r`#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int floorMod(int x, int m) {
    return ((x % m) + m) % m;
}

string trim(const string &s) {
    size_t first = s.find_first_not_of(" \t\r");
    size_t last = s.find_last_not_of(" \t\r");
    return first == string::npos ? "" : s.substr(first, last - first + 1);
}

int main() {
    string mode, key, text;
    getline(cin, mode);
    getline(cin, key);
    getline(cin, text);
    mode = trim(mode);
    key = trim(key);
    for (char &ch : key) ch = (char) toupper((unsigned char) ch);
    int sign = mode == "E" ? 1 : -1;
    size_t j = 0;
    for (char &ch : text) {
        bool upper = ch >= 'A' && ch <= 'Z', lower = ch >= 'a' && ch <= 'z';
        if (upper || lower) {
            char base = upper ? 'A' : 'a';
            int shift = sign * (key[j % key.size()] - 'A');
            ch = (char) (base + floorMod(ch - base + shift, 26));
            j++;
        }
    }
    cout << text << endl;
    return 0;
}
`,
  },
  'KIA-A7': {
    starter: r`#include <iostream>
using namespace std;

typedef long long ll;

// ...
ll mulmod(ll a, ll b, ll m) {
    return (ll) ((__int128) a * b % m);
}

ll modPow(ll base, ll exp, ll m) {
    // ...
    return 0;
}

ll modInverse(ll e, ll phi) {
    // ...
    return 0;
}

int main() {
    ll p, q, e, m;
    cin >> p >> q >> e >> m;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

typedef long long ll;

// (a * b) mod m — koʻpaytma 64 bitga sigʻmasligi mumkin, shuning uchun 128 bitli tur
ll mulmod(ll a, ll b, ll m) {
    return (ll) ((__int128) a * b % m);
}

ll modPow(ll base, ll exp, ll m) {
    ll result = 1 % m;
    base %= m;
    while (exp > 0) {
        if (exp & 1) result = mulmod(result, base, m);
        base = mulmod(base, base, m);
        exp >>= 1;
    }
    return result;
}

// Kengaytirilgan Evklid algoritmi: e * x ≡ 1 (mod phi)
ll modInverse(ll e, ll phi) {
    ll r0 = phi, r1 = e % phi, x0 = 0, x1 = 1;
    while (r1 != 0) {
        ll quotient = r0 / r1;
        ll r2 = r0 - quotient * r1;
        ll x2 = x0 - quotient * x1;
        r0 = r1;
        r1 = r2;
        x0 = x1;
        x1 = x2;
    }
    return ((x0 % phi) + phi) % phi;
}

int main() {
    ll p, q, e, m;
    cin >> p >> q >> e >> m;
    ll n = p * q;
    ll phi = (p - 1) * (q - 1);
    ll d = modInverse(e, phi);
    ll c = modPow(m, e, n);
    cout << n << " " << phi << " " << d << " " << c << " " << modPow(c, d, n) << endl;
    return 0;
}
`,
  },
  'KIA-A8': {
    starter: r`#include <cctype>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// ...
static const uint32_t K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

static uint32_t rotr(uint32_t x, int n) {
    return (x >> n) | (x << (32 - n));
}

// ...
string sha256(const string &message) {
    uint32_t h[8] = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
                     0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
    vector<uint8_t> data(message.begin(), message.end());
    uint64_t bitLength = (uint64_t) data.size() * 8;
    data.push_back(0x80);
    while (data.size() % 64 != 56) data.push_back(0);
    for (int i = 7; i >= 0; i--) data.push_back((uint8_t) (bitLength >> (8 * i)));

    for (size_t offset = 0; offset < data.size(); offset += 64) {
        uint32_t w[64];
        for (int i = 0; i < 16; i++) {
            w[i] = ((uint32_t) data[offset + 4 * i] << 24) | ((uint32_t) data[offset + 4 * i + 1] << 16) |
                   ((uint32_t) data[offset + 4 * i + 2] << 8) | (uint32_t) data[offset + 4 * i + 3];
        }
        for (int i = 16; i < 64; i++) {
            uint32_t s0 = rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >> 3);
            uint32_t s1 = rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >> 10);
            w[i] = w[i - 16] + s0 + w[i - 7] + s1;
        }
        uint32_t a = h[0], b = h[1], c = h[2], d = h[3], e = h[4], f = h[5], g = h[6], hh = h[7];
        for (int i = 0; i < 64; i++) {
            uint32_t S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
            uint32_t ch = (e & f) ^ (~e & g);
            uint32_t t1 = hh + S1 + ch + K[i] + w[i];
            uint32_t S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
            uint32_t maj = (a & b) ^ (a & c) ^ (b & c);
            uint32_t t2 = S0 + maj;
            hh = g;
            g = f;
            f = e;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }
        h[0] += a;
        h[1] += b;
        h[2] += c;
        h[3] += d;
        h[4] += e;
        h[5] += f;
        h[6] += g;
        h[7] += hh;
    }
    ostringstream out;
    for (uint32_t v : h) out << hex << setw(8) << setfill('0') << v;
    return out.str();
}

int main() {
    string message, expected;
    getline(cin, message);
    getline(cin, expected);
    // ...
    return 0;
}
`,
    solution: r`#include <cctype>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// C++ standart kutubxonasida SHA-256 yoʻq, shuning uchun algoritm (FIPS 180-4) shu yerda berilgan.
static const uint32_t K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

static uint32_t rotr(uint32_t x, int n) {
    return (x >> n) | (x << (32 - n));
}

// Xabarning SHA-256 xesh qiymati: 64 ta oʻn oltilik raqam
string sha256(const string &message) {
    uint32_t h[8] = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
                     0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
    vector<uint8_t> data(message.begin(), message.end());
    uint64_t bitLength = (uint64_t) data.size() * 8;
    data.push_back(0x80);
    while (data.size() % 64 != 56) data.push_back(0);
    for (int i = 7; i >= 0; i--) data.push_back((uint8_t) (bitLength >> (8 * i)));

    for (size_t offset = 0; offset < data.size(); offset += 64) {
        uint32_t w[64];
        for (int i = 0; i < 16; i++) {
            w[i] = ((uint32_t) data[offset + 4 * i] << 24) | ((uint32_t) data[offset + 4 * i + 1] << 16) |
                   ((uint32_t) data[offset + 4 * i + 2] << 8) | (uint32_t) data[offset + 4 * i + 3];
        }
        for (int i = 16; i < 64; i++) {
            uint32_t s0 = rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >> 3);
            uint32_t s1 = rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >> 10);
            w[i] = w[i - 16] + s0 + w[i - 7] + s1;
        }
        uint32_t a = h[0], b = h[1], c = h[2], d = h[3], e = h[4], f = h[5], g = h[6], hh = h[7];
        for (int i = 0; i < 64; i++) {
            uint32_t S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
            uint32_t ch = (e & f) ^ (~e & g);
            uint32_t t1 = hh + S1 + ch + K[i] + w[i];
            uint32_t S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
            uint32_t maj = (a & b) ^ (a & c) ^ (b & c);
            uint32_t t2 = S0 + maj;
            hh = g;
            g = f;
            f = e;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }
        h[0] += a;
        h[1] += b;
        h[2] += c;
        h[3] += d;
        h[4] += e;
        h[5] += f;
        h[6] += g;
        h[7] += hh;
    }
    ostringstream out;
    for (uint32_t v : h) out << hex << setw(8) << setfill('0') << v;
    return out.str();
}

int main() {
    string message, expected;
    getline(cin, message);
    getline(cin, expected);
    string clean;
    for (unsigned char ch : expected)
        if (!isspace(ch)) clean += (char) tolower(ch);
    string digest = sha256(message);
    cout << digest << "\n";
    cout << (digest == clean ? "Yaxlit" : "Buzilgan") << endl;
    return 0;
}
`,
  },
  'KIA-A10': {
    starter: r`#include <cctype>
#include <iostream>
#include <set>
#include <string>
using namespace std;

const set<string> COMMON = {"password", "123456", "12345678", "qwerty", "admin", "parol"};

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <cctype>
#include <iostream>
#include <set>
#include <string>
using namespace std;

const set<string> COMMON = {"password", "123456", "12345678", "qwerty", "admin", "parol"};

int score(const string &pw) {
    string lowered;
    for (unsigned char c : pw) lowered += (char) tolower(c);
    if (COMMON.count(lowered)) return 0;
    bool lower = false, upper = false, digit = false, special = false;
    for (unsigned char c : pw) {
        if (islower(c)) lower = true;
        else if (isupper(c)) upper = true;
        else if (isdigit(c)) digit = true;
        else special = true;
    }
    int s = 0;
    if (pw.size() >= 8) s++;
    if (pw.size() >= 12) s++;
    if (lower) s++;
    if (upper) s++;
    if (digit) s++;
    if (special) s++;
    return s;
}

int main() {
    int n;
    cin >> n;
    for (int k = 0; k < n; k++) {
        string pw;
        cin >> pw;
        int s = score(pw);
        cout << s << " " << (s <= 2 ? "Zaif" : s <= 4 ? "O'rtacha" : "Kuchli") << "\n";
    }
    return 0;
}
`,
  },
  'KIA-A11': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    for (int k = 0; k < n; k++) {
        string s;
        cin >> s;
        string digits;
        for (int g = 0; g < 3; g++) {
            int v = 0;
            for (int i = 1 + 3 * g; i < 4 + 3 * g; i++) {
                char c = s[i];
                v += c == 'r' ? 4 : c == 'w' ? 2 : c == 'x' ? 1 : 0;
            }
            digits += (char) ('0' + v);
        }
        string kind = s[0] == 'd' ? "katalog" : s[0] == 'l' ? "havola" : "fayl";
        cout << digits << " " << kind << "\n";
    }
    return 0;
}
`,
  },

  // ============================================================================ KOT1306
  'KOT-A4': {
    starter: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string scheme, bits;
    cin >> scheme >> bits;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
#include <string>
using namespace std;

int main() {
    string scheme, bits;
    cin >> scheme >> bits;
    string out;
    char level = '-';
    for (char b : bits) {
        if (scheme == "NRZ") {
            out += b == '1' ? '+' : '-';
        } else if (scheme == "NRZI") {
            if (b == '1') level = level == '-' ? '+' : '-';
            out += level;
        } else {
            out += b == '1' ? "-+" : "+-";
        }
    }
    cout << out << endl;
    return 0;
}
`,
  },
  'KOT-A7': {
    starter: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

// ...
uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

string str(uint32_t v) {
    return to_string((v >> 24) & 255) + "." + to_string((v >> 16) & 255) + "." +
           to_string((v >> 8) & 255) + "." + to_string(v & 255);
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

int main() {
    string cidr;
    cin >> cidr;
    size_t slash = cidr.find('/');
    string address = cidr.substr(0, slash);
    int prefix = atoi(cidr.substr(slash + 1).c_str());
    // ...
    return 0;
}
`,
    solution: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

// "192.168.10.77" -> 32 bitli son
uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

string str(uint32_t v) {
    return to_string((v >> 24) & 255) + "." + to_string((v >> 16) & 255) + "." +
           to_string((v >> 8) & 255) + "." + to_string(v & 255);
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

int main() {
    string cidr;
    cin >> cidr;
    size_t slash = cidr.find('/');
    string address = cidr.substr(0, slash);
    int prefix = atoi(cidr.substr(slash + 1).c_str());
    uint32_t m = mask(prefix);
    uint32_t net = ip(address) & m;
    uint32_t bc = net | ~m;
    cout << "Tarmoq: " << str(net) << "\n";
    cout << "Maska: " << str(m) << "\n";
    cout << "Broadcast: " << str(bc) << "\n";
    cout << "Birinchi xost: " << str(net + 1) << "\n";
    cout << "Oxirgi xost: " << str(bc - 1) << "\n";
    cout << "Xostlar soni: " << (1LL << (32 - prefix)) - 2 << "\n";
    return 0;
}
`,
  },
  'KOT-A8': {
    starter: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

int main() {
    int n;
    cin >> n;
    vector<uint32_t> nets(n), masks(n);
    vector<int> lens(n);
    vector<string> hops(n);
    for (int i = 0; i < n; i++) {
        string cidr;
        cin >> cidr >> hops[i];
        size_t slash = cidr.find('/');
        lens[i] = atoi(cidr.substr(slash + 1).c_str());
        masks[i] = mask(lens[i]);
        nets[i] = ip(cidr.substr(0, slash)) & masks[i];
    }
    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string address;
        cin >> address;
        uint32_t v = ip(address);
        int best = -1;
        for (int i = 0; i < n; i++)
            if ((v & masks[i]) == nets[i] && (best < 0 || lens[i] > lens[best])) best = i;
        cout << (best < 0 ? "DROP" : hops[best]) << "\n";
    }
    return 0;
}
`,
  },
  'KOT-A9': {
    starter: r`#include <cctype>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
using namespace std;

string trim(const string &s) {
    size_t first = s.find_first_not_of(" \t\r");
    size_t last = s.find_last_not_of(" \t\r");
    return first == string::npos ? "" : s.substr(first, last - first + 1);
}

int main() {
    string line;
    getline(cin, line);
    istringstream requestLine(trim(line));
    string method, target, version;
    requestLine >> method >> target >> version;
    // ...
    return 0;
}
`,
    solution: r`#include <cctype>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
using namespace std;

string trim(const string &s) {
    size_t first = s.find_first_not_of(" \t\r");
    size_t last = s.find_last_not_of(" \t\r");
    return first == string::npos ? "" : s.substr(first, last - first + 1);
}

int main() {
    string line;
    getline(cin, line);
    istringstream requestLine(trim(line));
    string method, target, version;
    requestLine >> method >> target >> version;

    size_t qm = target.find('?');
    string path = qm == string::npos ? target : target.substr(0, qm);
    string query = qm == string::npos || qm == target.size() - 1 ? "-" : target.substr(qm + 1);

    map<string, string> headers;
    while (getline(cin, line) && !trim(line).empty()) {
        size_t colon = line.find(':');
        string name = trim(line.substr(0, colon));
        for (char &ch : name) ch = (char) tolower((unsigned char) ch);
        headers[name] = trim(line.substr(colon + 1));
    }

    cout << "Metod: " << method << "\n";
    cout << "Yo'l: " << path << "\n";
    cout << "So'rov: " << query << "\n";
    cout << "Versiya: " << version << "\n";
    cout << "Host: " << (headers.count("host") ? headers["host"] : "-") << "\n";
    cout << "Sarlavhalar: " << headers.size() << "\n";
    return 0;
}
`,
  },
  'KOT-A14': {
    starter: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

struct Rule {
    string action, proto, port, cidr;
};

int main() {
    int n;
    cin >> n;
    // ...
    return 0;
}
`,
    solution: r`#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t ip(const string &s) {
    istringstream in(s);
    string part;
    uint32_t v = 0;
    while (getline(in, part, '.')) v = (v << 8) | (uint32_t) atoi(part.c_str());
    return v;
}

uint32_t mask(int len) {
    return len == 0 ? 0 : 0xFFFFFFFFu << (32 - len);
}

bool inCidr(const string &address, const string &cidr) {
    size_t slash = cidr.find('/');
    uint32_t m = mask(atoi(cidr.substr(slash + 1).c_str()));
    return (ip(address) & m) == (ip(cidr.substr(0, slash)) & m);
}

struct Rule {
    string action, proto, port, cidr;
};

int main() {
    int n;
    cin >> n;
    vector<Rule> rules(n);
    for (auto &r : rules) cin >> r.action >> r.proto >> r.port >> r.cidr;
    int m;
    cin >> m;
    for (int k = 0; k < m; k++) {
        string proto, port, src;
        cin >> proto >> port >> src;
        string verdict = "DENY default";
        for (int i = 0; i < n; i++) {
            const Rule &r = rules[i];
            if ((r.proto == "any" || r.proto == proto) && (r.port == "*" || r.port == port) && inCidr(src, r.cidr)) {
                verdict = r.action + " " + to_string(i + 1);
                break;
            }
        }
        cout << verdict << "\n";
    }
    return 0;
}
`,
  },

  // ============================================================================ IAKTT1105
  'IAKTT-S8': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        long long amount;
        cin >> amount;
        // ...
    }
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        long long amount;
        cin >> amount;
        long long discount;
        if (amount >= 1000000) discount = amount * 10 / 100;
        else if (amount >= 500000) discount = amount * 5 / 100;
        else discount = 0;
        cout << discount << " " << amount - discount << "\n";
    }
    return 0;
}
`,
  },
  'IAKTT-S15': {
    starter: r`#include <iostream>
using namespace std;

int main() {
    long long principal;
    int rate, years;
    cin >> principal >> rate >> years;
    long long balance = principal;
    // ...
    return 0;
}
`,
    solution: r`#include <iostream>
using namespace std;

int main() {
    long long principal;
    int rate, years;
    cin >> principal >> rate >> years;
    long long balance = principal;
    for (int year = 1; year <= years; year++) {
        balance += balance * rate / 100;
        cout << year << " " << balance << "\n";
    }
    cout << "Oddiy: " << principal + principal * rate * years / 100 << endl;
    return 0;
}
`,
  },
}
