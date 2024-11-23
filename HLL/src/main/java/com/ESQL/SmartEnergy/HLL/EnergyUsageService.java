package com.ESQL.SmartEnergy.HLL;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Scanner;

public class EnergyUsageService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/energy_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";

    public void createEnergyUsageTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "CREATE TABLE EnergyUsage (" +
                    "energy_usageId INT AUTO_INCREMENT PRIMARY KEY, " +
                    "deviceId INT, " +
                    "date DATE, " +
                    "usageHours FLOAT," +
                    "cost FLOAT," +
                    "FOREIGN KEY (deviceId) REFERENCES Device(deviceId) ON DELETE CASCADE)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            System.out.println("EnergyUsage 테이블이 성공적으로 생성되었습니다.");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기

            if (sqlCode == 1050) {
                System.out.println("EnergyUsage 테이블이 이미 존재합니다."); // MySQL 에러 코드 1050 처리
            } else {
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }
    }

    public float getPricePerKWh(int energyRating) {
        return switch (energyRating) {
            case 1 -> 100.0f;
            case 2 -> 150.0f;
            case 3 -> 200.0f;
            case 4 -> 250.0f;
            case 5 -> 300.0f;
            default -> 0.0f;
        };
    }

    public void addEnergyUsage(Scanner scanner, int userId) {
        int deviceId;

        while (true) {
            System.out.print("기기 고유 번호를 입력하세요: ");
            deviceId = scanner.nextInt();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                // deviceId가 userId의 소유인지 확인하는 쿼리 실행
                String checkSql = "SELECT COUNT(*) FROM Device WHERE deviceId = ? AND userId = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkSql);
                checkStatement.setInt(1, deviceId);
                checkStatement.setInt(2, userId);
                ResultSet resultSet = checkStatement.executeQuery();

                // 기기가 사용자의 소유가 아닌 경우 처리
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    System.out.println("입력한 기기는 사용자의 소유가 아닙니다. 출력된 기기 리스트에서 선택해주세요.");
                } else {
                    break;
                }
            } catch (SQLException e) {
                int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
                String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }

        System.out.print("기기 사용 시간을 입력하세요 (시간 단위): ");
        float usageHours = scanner.nextFloat();

        System.out.print("사용 날짜를 입력하세요 (yyyy-MM-dd): ");
        String date = scanner.next();

        Date usageDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(date); // 문자열을 java.util.Date로 파싱
            usageDate = new Date(parsedDate.getTime()); // java.util.Date를 java.sql.Date로 변환
        } catch (Exception e) {
            System.out.println("날짜 형식이 잘못되었습니다. 다시 시도하세요.");
            return;
        }

        float powerConsumption = 0;
        int energyRating = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT powerConsumption, energyRating FROM Device WHERE deviceId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, deviceId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                powerConsumption = resultSet.getFloat("powerConsumption");  // 사용량 가져오기
                energyRating = resultSet.getInt("energyRating");  // 에너지 등급 가져오기
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }

        float cost = powerConsumption * getPricePerKWh(energyRating) * usageHours;

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO EnergyUsage (deviceId, date, usageHours, cost) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, deviceId);
            preparedStatement.setDate(2, usageDate);
            preparedStatement.setFloat(3, usageHours);
            preparedStatement.setFloat(4, cost);
            preparedStatement.executeUpdate();
            System.out.println("사용 기록 등록이 완료되었습니다!");

            updateAlertAfterUsage(deviceId, userId, usageDate);
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    private void updateAlertAfterUsage(int deviceId, int userId, Date usageDate) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 현재 월과 연도 계산
            LocalDate currentDate = usageDate.toLocalDate();
            int month = currentDate.getMonthValue();
            int year = currentDate.getYear();

            // 알람 해결 처리
            String checkSql = "SELECT COUNT(*) FROM EnergyUsage WHERE deviceId = ? AND MONTH(date) = ? AND YEAR(date) = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setInt(1, deviceId);
            checkStatement.setInt(2, month);
            checkStatement.setInt(3, year);
            ResultSet resultSet = checkStatement.executeQuery();

            // 기기 사용량이 추가되었으면 알람을 해결 처리
            if (resultSet.next() && resultSet.getInt(1) > 0 ) {
                String updateSql = "UPDATE Alert SET resolved = TRUE WHERE userId = ? AND deviceId = ? AND alertType = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, userId);
                updateStatement.setInt(2, deviceId);
                updateStatement.setInt(3, 3); // alertType == 3
                updateStatement.executeUpdate();
            }

        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public void calculateMonthlyUsage(int userId) {
        float totalMonthlyUsage = 0.0f;
        Scanner scanner = new Scanner(System.in);

        // 사용자에게 원하는 달 입력받기
        int month;
        while (true) {
            System.out.print("원하는 달을 입력하세요 (1 ~ 12): ");
            month = scanner.nextInt();

            if (month >= 1 && month <= 12) {
                break; // 유효한 값 입력 시 반복 종료
            } else {
                System.out.println("잘못된 입력입니다. 1에서 12 사이의 숫자를 입력하세요.");
            }
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 해당 유저의 모든 기기를 가져오는 쿼리
            String deviceQuery = "SELECT deviceId, powerConsumption FROM Device WHERE userId = ?";
            PreparedStatement deviceStatement = connection.prepareStatement(deviceQuery);
            deviceStatement.setInt(1, userId);
            ResultSet deviceResultSet = deviceStatement.executeQuery();

            // 각 기기에 대해 사용량 계산
            while (deviceResultSet.next()) {
                int deviceId = deviceResultSet.getInt("deviceId");
                float powerConsumption = deviceResultSet.getFloat("powerConsumption"); // 전력 소비량 (kWh)

                // 각 기기의 선택한 월 사용 시간을 가져오는 쿼리
                String usageQuery = "SELECT usageHours FROM EnergyUsage WHERE deviceId = ? AND MONTH(date) = ?";
                PreparedStatement usageStatement = connection.prepareStatement(usageQuery);
                usageStatement.setInt(1, deviceId);
                usageStatement.setInt(2, month);
                ResultSet usageResultSet = usageStatement.executeQuery();

                float monthlyUsageForDevice = 0.0f;
                while (usageResultSet.next()) {
                    float time = usageResultSet.getFloat("usageHours"); // 시간 (hours)
                    monthlyUsageForDevice += powerConsumption * time;
                }

                totalMonthlyUsage += monthlyUsageForDevice; // 기기의 월 사용량을 총 사용량에 더하기
            }
            System.out.println(month + "월 총 전력 사용량: " + totalMonthlyUsage + "kWh");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }
}