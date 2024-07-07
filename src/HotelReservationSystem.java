//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HotelReservationSystem {
    private static final String url = "jdbc:mysql://localhost:3306/oyo_db";
    private static final String username = "root";
    private static final String password = "monu8306627855@";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nHOTEL MANAGEMENT SYSTEM");
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("6. Check Room Availability");
                System.out.println("7. List All Available Rooms");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;

                    case 6:
                        checkRoomAvailability(connection, scanner);
                        break;
                    case 7:
                        listAllAvailableRooms(connection);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.print("Enter room number: ");
            int roomNumber = scanner.nextInt();

            if (!roomExists(connection, roomNumber)) {
                System.out.println("Room number " + roomNumber + " does not exist. Please choose a valid room number.");
                return;
            }

            System.out.print("Enter contact number: ");
            String contactNumber = scanner.next();

            String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, guestName);
                statement.setInt(2, roomNumber);
                statement.setString(3, contactNumber);

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Reservation successful!");
                } else {
                    System.out.println("Reservation failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean roomExists(Connection connection, int roomNumber) {
        try {
            String sql = "SELECT room_number FROM rooms WHERE room_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, roomNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next(); // Returns true if room number exists
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Handle database errors as needed
        }
    }

    private static void viewReservations(Connection connection) {
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number      | Reservation Date        |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s   |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }

            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        String sql = "SELECT room_number FROM reservations WHERE reservation_id = ? AND guest_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            System.out.print("Enter reservation ID: ");
            int reservationId = scanner.nextInt();
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();

            pstmt.setInt(1, reservationId);
            pstmt.setString(2, guestName);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for Reservation ID " + reservationId +
                            " and Guest " + guestName + " is: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        String sql = "UPDATE reservations SET guest_name = ?, room_number = ?, contact_number = ? WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            System.out.print("Enter reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = scanner.nextLine();
            System.out.print("Enter new room number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = scanner.next();

            pstmt.setString(1, newGuestName);
            pstmt.setInt(2, newRoomNumber);
            pstmt.setString(3, newContactNumber);
            pstmt.setInt(4, reservationId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Reservation updated successfully!");
            } else {
                System.out.println("Reservation update failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteReservation(Connection connection, Scanner scanner) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            pstmt.setInt(1, reservationId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Reservation deleted successfully!");
            } else {
                System.out.println("Reservation deletion failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static void checkRoomAvailability(Connection connection, Scanner scanner) {
        String sql = "SELECT COUNT(*) AS count FROM reservations WHERE room_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            System.out.print("Enter room number to check availability: ");
            int roomNumber = scanner.nextInt();

            pstmt.setInt(1, roomNumber);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    if (count == 0) {
                        System.out.println("Room number " + roomNumber + " is available.");
                    } else {
                        System.out.println("Room number " + roomNumber + " is already reserved.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listAllAvailableRooms(Connection connection) {
        String sql = "SELECT room_number FROM rooms WHERE room_number NOT IN (SELECT room_number FROM reservations)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {

            System.out.println("Available Rooms:");
            System.out.println("+---------------+");
            System.out.println("| Room Number   |");
            System.out.println("+---------------+");

            while (resultSet.next()) {
                int roomNumber = resultSet.getInt("room_number");
                System.out.printf("| %-13d |\n", roomNumber);
            }

            System.out.println("+---------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("Thank You For Using Hotel Reservation System!!!");
    }
}


