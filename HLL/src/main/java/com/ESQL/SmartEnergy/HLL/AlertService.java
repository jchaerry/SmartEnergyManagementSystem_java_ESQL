package com.ESQL.SmartEnergy.HLL;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class AlertService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/energy_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // password

    public void createAlertTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "CREATE TABLE Alert (" +
                    "alertId INT AUTO_INCREMENT PRIMARY KEY, " +
                    "userId INT, " +
                    "deviceId INT, " +
                    "alertDate DATE, " +
                    "alertType INT, " +
                    "resolved BOOLEAN DEFAULT FALSE, " +
                    "FOREIGN KEY (deviceId) REFERENCES Device(deviceId) ON DELETE CASCADE, " +
                    "FOREIGN KEY (userId) REFERENCES User(userId) ON DELETE CASCADE)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            System.out.println("Alert 테이블이 성공적으로 생성되었습니다.");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기

            if (sqlCode == 1050) {
                System.out.println("Alert 테이블이 이미 존재합니다."); // MySQL 에러 코드 1050 처리
            } else {
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }
    }

    public String alertMent(int alertType, int deviceId, String deviceType) {
        return switch (alertType) {
            case 1 -> "이번 달 사용량이 10kWh를 초과했습니다. 에너지 절약을 고려해보세요.";
            case 2 -> "이번 달 예상 전기 요금이 30,000원을 초과했습니다. 사용 패턴을 점검해보세요.";
            case 3 -> "기기 번호 " + deviceId + "(" + deviceType +")" + "가(이) 한 달간 사용되지 않았습니다. 필요 없는 기기라면 제거를 고려해보세요.";
            case 4 -> "기기 번호 " + deviceId + "(" + deviceType +")" + "가(이) 평균 사용량보다 30% 더 높은 전력을 소비 중입니다. 점검을 권장합니다.";
            default -> "해당 알람이 없습니다.";
        };
    }

    // 평균 사용량 계산
    private float averageUsage(int deviceId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT d.powerConsumption, e.usageHours " +
                    "FROM Device d " +
                    "JOIN EnergyUsage e ON d.deviceId = e.deviceId " +
                    "WHERE d.deviceId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, deviceId);
            ResultSet resultSet = preparedStatement.executeQuery();

            float totalEnergyUsage = 0;
            int count = 0;

            while (resultSet.next()) {
                float powerConsumption = resultSet.getFloat("powerConsumption");
                float usageHours = resultSet.getFloat("usageHours");
                totalEnergyUsage += (powerConsumption * usageHours);
                count++;
            }

            if (count > 0) {
                return totalEnergyUsage / count;
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
        return 0;
    }

    private boolean isUsedThisMonth(String usageDate) {
        // 현재 날짜를 기준으로 한 달 전 날짜를 계산
        LocalDate currentDate = LocalDate.now();
        LocalDate oneMonthAgo = currentDate.minusMonths(1);
        LocalDate usageLocalDate = LocalDate.parse(usageDate);

        // 사용일이 한 달 이내이면 true 반환
        return !usageLocalDate.isBefore(oneMonthAgo);
    }

    public void checkAndGenerateAlerts(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 사용자의 기기 목록 조회
            String deviceSql = "SELECT deviceId, powerConsumption FROM Device WHERE userId = ?";
            PreparedStatement deviceStatement = connection.prepareStatement(deviceSql);
            deviceStatement.setInt(1, userId);
            ResultSet deviceResult = deviceStatement.executeQuery();

            LocalDate currentDate = LocalDate.now();
            int currentMonth = currentDate.getMonthValue();

            while (deviceResult.next()) {
                int deviceId = deviceResult.getInt("deviceId");
                float powerConsumption = deviceResult.getFloat("powerConsumption"); // 전력 소비량 (kWh)
                // 평균 사용량 계산
                float averageUsage = averageUsage(deviceId);

                // 에너지 사용량 확인
                String usageSql = "SELECT usageHours, cost, date FROM EnergyUsage WHERE deviceId = ? AND MONTH(date) = ?";
                PreparedStatement usageStatement = connection.prepareStatement(usageSql);
                usageStatement.setInt(1, deviceId);
                usageStatement.setInt(2, currentMonth);
                ResultSet usageResult = usageStatement.executeQuery();

                float totalEnergyUsage = 0;
                float totalCost = 0;
                boolean usedThisMonth = false;

                while (usageResult.next()) {
                    float usageHours = usageResult.getFloat("usageHours");
                    float cost = usageResult.getFloat("cost");
                    String date = usageResult.getString("date");

                    // 한 달 이내 사용 기록이 있다면 usedThisMonth를 true로 설정
                    if (isUsedThisMonth(date)) {
                        usedThisMonth = true;
                    }

                    totalCost += cost;

                    totalEnergyUsage += powerConsumption * usageHours;
                }

                // 알람 조건 체크 및 중복 방지
                if (totalEnergyUsage > averageUsage * 1.3) {
                    generateAlert(userId, deviceId, 4); // 평균보다 30% 초과
                }
                if (totalCost > 30000) {
                    generateAlert(userId, deviceId, 2); // 예상 요금 초과
                }
                if (totalEnergyUsage > 10) {
                    generateAlert(userId, deviceId, 1); // 사용량 초과
                }
                if (!usedThisMonth) {
                    generateAlert(userId, deviceId, 3); // 한 달간 사용되지 않음
                }
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    private void generateAlert(int userId, int deviceId, int alertType) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 현재 날짜 계산
            LocalDate currentDate = LocalDate.now();
            LocalDate startDate;

            // 알람 유형에 따라 기준 날짜 설정
            if (alertType == 1 || alertType == 2 || alertType == 3) {
                // 매달 생성 가능한 알람: 이번 달의 첫 날
                startDate = currentDate.withDayOfMonth(1);
            } else if (alertType == 4) {
                // 매일 생성 가능한 알람: 하루 전
                startDate = currentDate.minusDays(1);
            } else {
                System.out.println("알 수 없는 알람 유형입니다.");
                return;
            }

            // 기존 알람 여부 확인
            String checkSql = "SELECT resolved FROM Alert WHERE userId = ? AND deviceId = ? AND alertType = ? AND alertDate >= ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setInt(1, userId);
            checkStatement.setInt(2, deviceId);
            checkStatement.setInt(3, alertType);
            checkStatement.setDate(4, Date.valueOf(startDate));

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                boolean resolved = resultSet.getBoolean("resolved");
                if (!resolved) {
                    return;
                }
                return;
            }

            // 새로운 알람 생성
            String insertSql = "INSERT INTO Alert (userId, deviceId, alertDate, alertType, resolved) " +
                    "VALUES (?, ?, CURRENT_DATE, ?, false)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setInt(1, userId);
            insertStatement.setInt(2, deviceId);
            insertStatement.setInt(3, alertType);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    // 알람 프린트 매소드
    public void displayAlerts(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT a.alertId, a.alertType, a.deviceId, d.deviceType " +
                    "FROM Alert a " +
                    "JOIN Device d ON a.deviceId = d.deviceId " +
                    "WHERE a.userId = ? AND a.resolved = FALSE";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int alertId = resultSet.getInt("alertId");
                int alertType = resultSet.getInt("alertType");
                int deviceId = resultSet.getInt("deviceId");
                String deviceType = resultSet.getString("deviceType");
                System.out.println(alertId + ". " + alertMent(alertType, deviceId, deviceType));
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    // 알림을 처리(해결)하는 메소드
    public void resolveAlert(Scanner scanner, int userId) {

        while (true) {
            System.out.print("읽음 처리할 알람 번호를 입력하세요: ");
            int alertId = scanner.nextInt();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                String sql = "UPDATE Alert SET resolved = TRUE WHERE alertId = ? AND userId = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, alertId);
                preparedStatement.setInt(2, userId);
                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("알람 번호 " + alertId + "번을 읽음 처리 하였습니다.");
                    break;
                } else {
                    System.out.println("해당 알람을 찾을 수 없거나, 권한이 없습니다.");
                }
            } catch (SQLException e) {
                int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
                String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }
    }
}
