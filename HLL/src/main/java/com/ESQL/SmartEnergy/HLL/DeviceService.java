package com.ESQL.SmartEnergy.HLL;

import java.sql.*;
import java.util.*;

public class DeviceService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/energy_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";

    public void createDeviceTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "CREATE TABLE Device (" +
                    "deviceId INT AUTO_INCREMENT PRIMARY KEY, " +
                    "userId INT, " +
                    "deviceType VARCHAR(50), " +
                    "energyRating INT," +
                    "powerConsumption FLOAT," +
                    "FOREIGN KEY (userId) REFERENCES User(userId) ON DELETE CASCADE)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            System.out.println("Device 테이블이 성공적으로 생성되었습니다.");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기

            if (sqlCode == 1050) {
                System.out.println("Device 테이블이 이미 존재합니다."); // MySQL 에러 코드 1050 처리
            } else {
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }
    }

    public List<Map<String, Object>> findAllDevice(int userId) {
        List<Map<String, Object>> devices = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT deviceId, deviceType, energyRating FROM Device WHERE userId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> deviceData = new HashMap<>();
                deviceData.put("기기 번호", resultSet.getInt("deviceId"));
                deviceData.put("기기 유형", resultSet.getString("deviceType"));
                deviceData.put("에너지 등급", resultSet.getString("energyRating"));
                devices.add(deviceData);
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
        return devices;
    }

    public void registerDevice(Scanner scanner, int userId) {
        System.out.print("기기 유형을 입력하세요: ");
        String deviceType = scanner.nextLine();

        double powerConsumption;
        while (true) {
            System.out.print("전력 소비량(kW)을 입력하세요: ");
            if (scanner.hasNextDouble()) {
                powerConsumption = scanner.nextDouble();
                if (powerConsumption > 0) {
                    break;  // 유효한 전력 소비량이 입력되면 반복 종료
                } else {
                    System.out.println("전력 소비량은 0보다 큰 값이어야 합니다.");
                }
            } else {
                System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
                scanner.next();  // 잘못된 입력 소비
            }
        }

        int energyRating;
        do {
            System.out.print("에너지 등급을 1에서 5까지 입력하세요: ");
            while (!scanner.hasNextInt()) {
                System.out.println("숫자만 입력 가능합니다.");
                scanner.nextLine();
            }
            energyRating = scanner.nextInt();
            scanner.nextLine();

            if (energyRating < 1 || energyRating > 5) {
                System.out.println("잘못된 입력입니다. 1에서 5까지의 숫자만 입력 가능합니다.");
            }
        } while (energyRating < 1 || energyRating > 5);


        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO Device (userId, deviceType, energyRating, powerConsumption) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,userId);
            preparedStatement.setString(2, deviceType);
            preparedStatement.setInt(3, energyRating);
            preparedStatement.setDouble(4, powerConsumption);
            preparedStatement.executeUpdate();
            System.out.println("기기 등록이 완료되었습니다!");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }
}
