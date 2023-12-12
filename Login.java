import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

class Transaction implements Serializable{
    private String date;
    private String transactionId;
    private double amount;
    private String description;
    public Transaction(double amount,String description) {
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.transactionId = generateTransactionId();
        this.amount = amount;
        this.description = description;
    }


    public String getDate() {
        return date;
    }

    public String getTransactionId() {
        return transactionId;
    }

    private String generateTransactionId() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000));
    }
}

class Receipt {
    private String date;
    private String transactionId;
    private String details;

    public Receipt(String date, String transactionId, String details) {
        this.date = date;
        this.transactionId = transactionId;
        this.details = details;
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Date: ").append(date).append("\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n");
        receipt.append(details).append("\n");
        return receipt.toString();
    }
}

class BankAccountManager {
    private Map<String, BankAccount> accountMap; // Map username to BankAccount
    private String filePath;

    public BankAccountManager(String filePath) {
        this.filePath = filePath;
        this.accountMap = loadAccountMap();
    }

    public void saveAccountDetails(BankAccount bankAccount) {
        // Add or update the account in the map
        accountMap.put(bankAccount.getAccountHolder(), bankAccount);
        saveAccountMap();
    }

    public BankAccount getAccount(String username) {
        return accountMap.get(username);
    }

    public BankAccount getAccount(int accountNumber) {
        // You might need to iterate through the map to find the account with the specified account number
        for (BankAccount account : accountMap.values()) {
            if (account.getAccountNumber() == accountNumber) {
                return account;
            }
        }
        return null;
    }

    private void saveAccountMap() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(accountMap);
            System.out.println("Account map saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving account map: " + e.getMessage());
        }
    }

    private Map<String, BankAccount> loadAccountMap() {
        Map<String, BankAccount> loadedMap = new HashMap<>();
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = inputStream.readObject();
            if (obj instanceof Map) {
                loadedMap = (Map<String, BankAccount>) obj;
                System.out.println("Account map loaded successfully.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error loading account map: " + e.getMessage());
        }
        return loadedMap;
    }
}

class BankAccount implements Serializable{
    protected int accountNumber;
    protected String accountHolder;
    protected double balance;
    protected ArrayList<Transaction> transactionHistory;

    public BankAccount(int accountNumber, String accountHolder) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = 0;
        this.transactionHistory = new ArrayList<>();
    }

    public BankAccount(int accountNumber, String accountHolder, int balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
        this.transactionHistory.add(new Transaction(balance,"Initial Transaction")); 
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactionHistory;
    }

    public void deposit(double amount) {
        balance += amount;
        transactionHistory.add(new Transaction(amount,"Deposit"));
    }

    public boolean withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            transactionHistory.add(new Transaction(amount,"Withdrawl"));
            return true;
        } else {
            System.out.println("Insufficient funds");
            return false;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}

class SavingsAccount extends BankAccount {
    private double interestRate;

    public SavingsAccount(int accountNumber, String accountHolder, int balance, double interestRate) {
        super(accountNumber, accountHolder, balance);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }
}

class LoanMortgageAccount extends BankAccount implements Serializable {
    private double loanAmount;
    private double interestRate;
    private int loanTermMonths; // The duration of the loan in months

    public LoanMortgageAccount(int accountNumber, String accountHolder, double loanAmount, double interestRate, int loanTermMonths) {
        super(accountNumber, accountHolder);
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanTermMonths = loanTermMonths;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public int getLoanTermMonths() {
        return loanTermMonths;
    }

    @Override
    public boolean withdraw(double amount) {
        // In a loan mortgage account, withdrawing is not applicable. You might want to handle this accordingly.
        System.out.println("Withdrawal not allowed for Loan Mortgage Account.");
        return false;
    }

    public void makeLoanPayment(double paymentAmount) {
        double monthlyInterest = loanAmount * interestRate / 12;
        double monthlyPayment = calculateMonthlyPayment();

        if (paymentAmount >= monthlyPayment) {
            // Valid payment
            balance -= paymentAmount;
            loanAmount -= (paymentAmount - monthlyInterest);

            // Record the transaction with the current date
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String transactionDate = dateFormat.format(currentDate);

            transactionHistory.add(new Transaction(paymentAmount, "Loan Payment"));
            System.out.println("Loan payment successful.");
        } else {
            System.out.println("Invalid payment amount. Please pay at least the monthly payment.");
        }
    }

    // Additional method to calculate the monthly payment
    private double calculateMonthlyPayment() {
        double monthlyInterestRate = interestRate / 12;
        int numberOfPayments = loanTermMonths;
        double base = 1 + monthlyInterestRate;
        double power = Math.pow(base, numberOfPayments);

        double monthlyPayment = (loanAmount * monthlyInterestRate * power) / (power - 1);
        return monthlyPayment;
    }
}

class CheckingAccount extends BankAccount {
    private double overdraftLimit;

    public CheckingAccount(int accountNumber, String accountHolder, int balance, double overdraftLimit) {
        super(accountNumber, accountHolder, balance);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= balance + overdraftLimit) {
            balance -= amount;
            transactionHistory.add(new Transaction(amount,"Withdraw"));
            return true;
        } else {
            System.out.println("Withdrawal amount exceeds overdraft limit");
            return false;
        }
    }
}

class RegisterForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField phonenumberField;
    private Map<String, String> userCredentials;

    class Person {
        private String username;
        private String password;
        private String email;
        private String phonenumber;

        public Person(String username_, String password_, String email_, String phonenumber_) {
            username = username_;
            password = password_;
            email = email_;
            phonenumber = phonenumber_;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }

        public String getPhonenumber() {
            return phonenumber;
        }

        public void setPassword(String password_) {
            password = password_;
        }

        public void setEmail(String email_) {
            email = email_;
        }

        public void setPhonenumber(String phonenumber_) {
            phonenumber = phonenumber_;
        }

        public boolean verifyPassword(String password_) {
            return password.equals(password_);
        }
    }

    public RegisterForm() {
        userCredentials = new HashMap<>(); // Initialize the userCredentials map

        // Initialize Swing components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        emailField = new JTextField(20);
        phonenumberField = new JTextField(20);

        JButton registerButton = new JButton("Register");

        // Create a panel for components
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phonenumberField);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(registerButton);

        // Add the panel to the center of the frame
        add(panel, BorderLayout.CENTER);

        // Set up action listener
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // Set up frame properties
        setTitle("Register Form");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();
        String phonenumber = phonenumberField.getText();

        // Create a BankAccount based on the selected account type (Savings, LoanMortgage, or Checking)
        String filePath = "accountDetails.ser";
        BankAccountManager accountManager = new BankAccountManager(filePath);
        BankAccount bankAccount = createBankAccount();
        accountManager.saveAccountDetails(bankAccount);
        // Store the details in a file
        saveUserDetails(username, password, email, phonenumber, bankAccount);

        JOptionPane.showMessageDialog(this, "Registration successful!");
        clearFields();
    }

    private BankAccount createBankAccount() {
        // Logic to determine the selected account type and create the corresponding BankAccount
        // For simplicity, I'm assuming a default SavingsAccount here. Modify as needed.
        return new SavingsAccount(123, usernameField.getText(), 500, 0.02);
    }

    private void saveUserDetails(String username, String password, String email, String phonenumber, BankAccount bankAccount) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("user_details.txt", true))) {
        writer.write("Username: " + username + "\n");
        writer.write("Password: " + password + "\n");
        writer.write("Email: " + email + "\n");
        writer.write("Phone Number: " + phonenumber + "\n");
        
        // Save additional details for the BankAccount
        writer.write("Account Number: " + bankAccount.getAccountNumber() + "\n");
        writer.write("Balance: " + bankAccount.getBalance() + "\n");
        // Add more details as needed
        
        writer.write("\n"); // Add a newline for separation
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error saving user details", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        phonenumberField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RegisterForm();
            }
        });
    }
}

class BankApplication extends JFrame {
    private BankAccount bankAccount;
    private JTextArea receiptArea;

    public BankApplication(BankAccount bankAccount) {
        this.bankAccount = bankAccount;

        // Initialize Swing components
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        receiptArea = new JTextArea(10, 30);
        receiptArea.setEditable(false);

        // Create a panel for components
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(depositButton);
        panel.add(withdrawButton);

        // Add the panel to the center of the frame
        add(panel, BorderLayout.CENTER);
        add(new JScrollPane(receiptArea), BorderLayout.SOUTH);

        // Set up action listeners
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeposit();
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleWithdraw();
            }
        });

        // Set up frame properties
        setTitle("Bank Application");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void handleDeposit() {
        double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter deposit amount:"));
        bankAccount.deposit(amount);

        Transaction transaction = new Transaction(amount,"Deposit");
        Receipt receipt = new Receipt(transaction.getDate(), transaction.getTransactionId(),"Deposit Amount: " + amount);
        updateReceiptArea(receipt.generateReceipt());
    }

    private void handleWithdraw() {
        double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter withdrawal amount:"));
        bankAccount.withdraw(amount);

        // Generate receipt and update the receipt area
        Transaction transaction = new Transaction(amount,"Withdrawl");
        Receipt receipt = new Receipt(transaction.getDate(), transaction.getTransactionId(),
                "Withdrawal Amount: " + amount);
        updateReceiptArea(receipt.generateReceipt());
    }

    private void updateReceiptArea(String receiptText) {
        receiptArea.setText(receiptText);
    }

    

    

    

}


public class Login extends JFrame {
    private static final String USER_FILE_PATH = "user_credentials.txt";
    private Map<String, String> userCredentials;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public Login() {
        userCredentials = loadUserCredentials();

        // Initialize Swing components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Create a panel for components
        JPanel panel = new JPanel(new GridLayout(3,2));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        // Add the panel to the center of the frame
        add(panel, BorderLayout.CENTER);

        // Set up action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // Set up frame properties
        setTitle("Bank Mini Project");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
            BankAccount bankAccount = loadBankAccountDetails(username);// Load the user's bank account details
            openAccountDetailsForm(bankAccount);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BankAccount loadBankAccountDetails(String username) {
        String filePath = "accountDetails.ser";
        BankAccountManager accountManager = new BankAccountManager(filePath);
        return accountManager.getAccount(username);
    }

    private void openAccountDetailsForm(BankAccount bankAccount) {
        // Create and open a new JFrame to display account details
        // Customize this part based on your application needs
        JFrame accountDetailsFrame = new JFrame("Account Details");
        JLabel balanceLabel = new JLabel("Balance: " + bankAccount.getBalance());
        JLabel accountHolderLabel = new JLabel("Account Holder : "+bankAccount.getAccountHolder());
        JLabel accountNumberLabel = new JLabel("Account Number : "+bankAccount.getAccountNumber());
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeposit(bankAccount);
                balanceLabel.setText("Balance: " + bankAccount.getBalance());
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleWithdraw(bankAccount);
                balanceLabel.setText("Balance: " + bankAccount.getBalance());
            }
        });

        JPanel panel = new JPanel(new GridLayout(5, 1));
        panel.add(accountHolderLabel);
        panel.add(accountNumberLabel);
        panel.add(balanceLabel);
        panel.add(depositButton);
        panel.add(withdrawButton);

        accountDetailsFrame.add(panel);
        accountDetailsFrame.setSize(300, 200);
        accountDetailsFrame.setLocationRelativeTo(null);
        accountDetailsFrame.setVisible(true);
    }

    private void handleDeposit(BankAccount bankAccount) {
        String depositAmountString = JOptionPane.showInputDialog("Enter deposit amount:");
        if (depositAmountString != null && !depositAmountString.isEmpty()) {
            double depositAmount = Double.parseDouble(depositAmountString);
            bankAccount.deposit(depositAmount);
            String filePath = "accountDetails.ser";
            BankAccountManager accountManager = new BankAccountManager(filePath);
            accountManager.saveAccountDetails(bankAccount);
        }
    }

    private void handleWithdraw(BankAccount bankAccount) {
        String withdrawAmountString = JOptionPane.showInputDialog("Enter withdrawal amount:");
        if (withdrawAmountString != null && !withdrawAmountString.isEmpty()) {
            double withdrawAmount = Double.parseDouble(withdrawAmountString);
            bankAccount.withdraw(withdrawAmount);
            String filePath = "accountDetails.ser";
            BankAccountManager accountManager = new BankAccountManager(filePath);
            accountManager.saveAccountDetails(bankAccount);
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (userCredentials.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            userCredentials.put(username, password);
            saveUserCredentials();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new RegisterForm();
                }
            });
        }
    }

    private Map<String, String> loadUserCredentials() {
        Map<String, String> credentials = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // If the file doesn't exist, create an empty file
            createEmptyFile();
        }

        return credentials;
    }

    private void createEmptyFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH))) {
            System.out.println("User credentials file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating the user credentials file: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error creating the user credentials file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUserCredentials() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
            System.out.println("User credentials saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving user credentials: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving user credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login();
            }
        });
    }
}
