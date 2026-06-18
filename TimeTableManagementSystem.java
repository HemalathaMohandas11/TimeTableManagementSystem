import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TimeTableManagementSystem {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

class DBConnection {

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.");
        }

        String url = "jdbc:mysql://localhost:3306/hemalatha";
        String user = "root";
        String password = "Hemalatha";

        return DriverManager.getConnection(url, user, password);
    }
}

class LoginFrame extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Time Table Management System");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        add(new JLabel());
        add(loginBtn);

        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt =
                    conn.prepareStatement("CALL ValidateUser(?, ?)");

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                int userId = rs.getInt("user_id");
                String role = rs.getString("role");

                dispose();

                new DashboardFrame(userId, role);

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid Username or Password"
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

class DashboardFrame extends JFrame {

    public DashboardFrame(int userId, String role) {

        setTitle(role.toUpperCase() + " Dashboard");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {
                "Day",
                "Time",
                "Subject",
                "Class"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        JScrollPane scrollPane =
                new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement stmt;

            if (role.equalsIgnoreCase("principal")) {

                stmt =
                        conn.prepareStatement(
                                "CALL GetAllSchedules()"
                        );

            } else {

                stmt =
                        conn.prepareStatement(
                                "CALL GetScheduleByUser(?)"
                        );

                stmt.setInt(1, userId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getString("day_of_week"),
                        rs.getString("period_time"),
                        rs.getString("subject"),
                        rs.getString("room")
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }

        if (role.equalsIgnoreCase("principal")) {

            JButton addButton =
                    new JButton("Add Schedule");

            addButton.addActionListener(
                    e -> AddScheduleDialog.showDialog()
            );

            add(addButton, BorderLayout.SOUTH);
        }

        setVisible(true);
    }
}

class AddScheduleDialog {

    public static void showDialog() {

        JTextField userIdField = new JTextField();
        JTextField dayField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextField classField = new JTextField();

        Object[] fields = {

                "User ID:", userIdField,

                "Day:", dayField,

                "Time (HH:MM:SS):", timeField,

                "Subject:", subjectField,

                "Class:", classField
        };

        int option = JOptionPane.showConfirmDialog(
                null,
                fields,
                "Add Schedule",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            try (Connection conn =
                         DBConnection.getConnection()) {

                PreparedStatement stmt =
                        conn.prepareStatement(
                                "CALL AddSchedule(?, ?, ?, ?, ?)"
                        );

                stmt.setInt(
                        1,
                        Integer.parseInt(
                                userIdField.getText()
                        )
                );

                stmt.setString(
                        2,
                        dayField.getText()
                );

                stmt.setTime(
                        3,
                        Time.valueOf(
                                timeField.getText()
                        )
                );

                stmt.setString(
                        4,
                        subjectField.getText()
                );

                stmt.setString(
                        5,
                        classField.getText()
                );

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        null,
                        "Schedule Added Successfully"
                );

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}