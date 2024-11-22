package com.ESQL.SmartEnergy.HLL;

import java.sql.*;
import java.util.Scanner;

public class CrossCheckService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/energy_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // password


    public void choiceCrossCheck(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("1. USER 테이블");
            System.out.println("2. DEVICE 테이블");
            System.out.println("3. EnergyUsage 테이블");
            System.out.println("4. Alert 테이블");
            System.out.println("5. 돌아가기");
            System.out.print("옵션 선택하기: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    userCrossCheck();
                    break;
                case 2:
                    deviceCrossCheck();
                    break;
                case 3:
                    energyUsageCrossCheck();
                    break;
                case 4:
                    alertCrossCheck();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("잘못된 옵션입니다. 다시 시도하세요.");
                    break;
            }
        }
    }

    public static void userCrossCheck() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM USER";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\nID\t이름\t전화번호\t비밀번호");
            System.out.println("------------------------------------");

            while (resultSet.next()) {
                System.out.println(resultSet.getInt("userId") + "\t" +
                        resultSet.getString("name") + "\t" +
                        resultSet.getString("phoneNumber") + "\t" +
                        resultSet.getString("password"));
            }
            System.out.println("------------------------------------");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public static void deviceCrossCheck() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM DEVICE";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\ndeviceID\tuserID\t기기 유형\t에너지 등급\t전력 소비량");
            System.out.println("------------------------------------");

            while (resultSet.next()) {
                System.out.println(resultSet.getInt("deviceId") + "\t" +
                        resultSet.getString("userId") + "\t" +
                        resultSet.getString("deviceType") + "\t" +
                        resultSet.getInt("energyRating") + "\t" +
                        resultSet.getFloat("powerConsumption"));
            }
            System.out.println("------------------------------------");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public static void energyUsageCrossCheck() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM EnergyUsage";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\nID\tdeviceID\t사용 날짜\t사용 시간\t비용");
            System.out.println("------------------------------------");

            while (resultSet.next()) {
                System.out.println(resultSet.getInt("energy_usageId") + "\t" +
                        resultSet.getString("deviceId") + "\t" +
                        resultSet.getDate("date") + "\t" +
                        resultSet.getFloat("usageHours") + "\t" +
                        resultSet.getFloat("cost"));
            }
            System.out.println("------------------------------------");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }
    public static void alertCrossCheck() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM Alert";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\nID\tuserID\tdeviceId\t알림 날짜\t알림 유형\t처리 여부");
            System.out.println("------------------------------------");

            while (resultSet.next()) {
                System.out.println(resultSet.getInt("alertId") + "\t" +
                        resultSet.getString("userId") + "\t" +
                        resultSet.getString("deviceId") + "\t" +
                        resultSet.getDate("alertDate") + "\t" +
                        resultSet.getInt("alertType") + "\t" +
                        resultSet.getBoolean("resolved"));
            }
            System.out.println("------------------------------------");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }
}
