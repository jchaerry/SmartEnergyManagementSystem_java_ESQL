package com.ESQL.SmartEnergy.HLL;

import java.sql.*;
import java.util.Scanner;

public class UserService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/energy_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // password

    public static int userId;

    public void createUserTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "CREATE TABLE User (" +
                    "userId INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "phoneNumber VARCHAR(15) UNIQUE, " +
                    "password VARCHAR(100))";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            System.out.println("User 테이블이 성공적으로 생성되었습니다.");
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기

            if (sqlCode == 1050) {
                System.out.println("User 테이블이 이미 존재합니다."); // MySQL 에러 코드 1050 처리
            } else {
                System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
                System.out.println("에러 메시지: " + e.getMessage());
            }
        }
    }

    public void registerUser(Scanner scanner) {
        System.out.print("이름을 입력하세요: ");
        String name = scanner.nextLine();
        String phoneNumber;

        // 전화번호 유효성 검사
        while (true) {
            System.out.print("전화번호를 입력하세요(형식: 010-xxxx-xxxx): ");
            phoneNumber = scanner.nextLine();

            if (phoneNumber.matches("\\d{3}-\\d{4}-\\d{4}")) {
                break; // 올바른 형식일 경우 반복 종료
            } else {
                System.out.println("전화번호 형식이 잘못되었습니다. 형식: 010-xxxx-xxxx");
            }
        }

        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 전화번호로 이미 가입된 사용자 확인
            String checkSql = "SELECT * FROM User WHERE phoneNumber = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, phoneNumber);
            ResultSet checkResultSet = checkStatement.executeQuery();

            if (checkResultSet.next()) {
                // 이미 가입된 경우
                System.out.println("이미 가입된 전화번호입니다.");
            } else {
                // 신규 사용자 등록
                String sql = "INSERT INTO User (name, phoneNumber, password) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, phoneNumber);
                preparedStatement.setString(3, password);
                preparedStatement.executeUpdate();
                System.out.println("회원 등록이 완료되었습니다!");
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public void findUser(Scanner scanner) {
        String searchPhone;

        // 전화번호 유효성 검사
        while (true) {
            System.out.print("전화번호를 입력하세요 (형식: 010-xxxx-xxxx): ");
            searchPhone = scanner.nextLine();

            if (searchPhone.matches("\\d{3}-\\d{4}-\\d{4}")) {
                break; // 올바른 형식일 경우 반복 종료
            } else {
                System.out.println("전화번호 형식이 잘못되었습니다. 형식: 010-xxxx-xxxx");
            }
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM User WHERE phoneNumber = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, searchPhone);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("이름: " + resultSet.getString("name"));
                System.out.println("전화번호: " + resultSet.getString("phoneNumber"));
            } else {
                System.out.println("해당 번호로 가입된 유저가 없습니다.");
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public void deleteUser(Scanner scanner) {
        String deletePhone;
        while (true) {
            System.out.print("전화번호를 입력하세요 (형식: 010-xxxx-xxxx): ");
            deletePhone = scanner.nextLine();

            if (deletePhone.matches("\\d{3}-\\d{4}-\\d{4}")) {
                break; // 올바른 형식일 경우 반복 종료
            } else {
                System.out.println("전화번호 형식이 잘못되었습니다. 형식: 010-xxxx-xxxx");
            }
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM User WHERE phoneNumber = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, deletePhone);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("회원 정보가 일치하는지 확인하세요");
                System.out.println("이름: " + resultSet.getString("name"));
                System.out.println("전화번호: " + resultSet.getString("phoneNumber"));

                while (true) {
                    System.out.println("1. 회원 삭제");
                    System.out.println("2. 취소");
                    System.out.print("옵션 선택하기: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.print("비밀번호를 입력하세요(취소하려면 '취소' 입력): ");
                            String deletePassword = scanner.nextLine();

                            if (deletePassword.equalsIgnoreCase("취소")) {
                                System.out.println("삭제 작업이 취소되었습니다.");
                                return;
                            }

                            String dsql = "DELETE FROM User WHERE phoneNumber = ? AND password = ?";
                            PreparedStatement deleteStatement = connection.prepareStatement(dsql);
                            deleteStatement.setString(1, deletePhone);
                            deleteStatement.setString(2, deletePassword);
                            int rowsAffected = deleteStatement.executeUpdate();

                            if (rowsAffected > 0) {
                                System.out.println("회원 정보가 삭제되었습니다.");
                                return;
                            } else {
                                System.out.println("비밀번호가 일치하지 않습니다.");
                                System.out.println("옵션을 다시 선택해주세요.");
                            }
                            break;

                        case 2:
                            System.out.println("삭제 작업이 취소되었습니다.");
                            return; // 취소 후 함수 종료

                        default:
                            System.out.println("잘못된 옵션입니다. 다시 시도하세요.");
                            break;
                    }
                }
            } else {
                System.out.println("해당 번호로 가입된 유저가 없습니다.");
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public void loginUser(Scanner scanner) {
        System.out.print("전화번호를 입력하세요 (형식: 010-xxxx-xxxx): ");
        String phoneNumber = scanner.nextLine();

        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // 전화번호와 비밀번호로 로그인 확인
            String sql = "SELECT userId FROM User WHERE phoneNumber = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, phoneNumber);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("userId"); // 로그인 성공 시 userId 저장
                System.out.println("로그인 성공!");
            } else {
                System.out.println("로그인 실패: 전화번호 또는 비밀번호가 일치하지 않습니다.");
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
    }

    public String getPhoneNumberByUserId(int userId) {
        String phoneNumber = null; // 전화번호를 저장할 변수
        if (userId == 0) {
            return "비로그인 상태입니다.";
        }
        String sql = "SELECT phoneNumber FROM User WHERE userId = ?"; // 유저 아이디로 전화번호 조회

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId); // 유저 아이디를 쿼리에 설정

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                phoneNumber = resultSet.getString("phoneNumber"); // 전화번호 가져오기
            } else {
                System.out.println("해당 ID로 등록된 유저가 없습니다.");
            }
        } catch (SQLException e) {
            int sqlCode = e.getErrorCode(); // SQLCODE 가져오기
            String sqlState = e.getSQLState(); // SQL 상태 코드 가져오기
            System.out.println("SQLCODE: " + sqlCode + ", SQL 상태: " + sqlState);
            System.out.println("에러 메시지: " + e.getMessage());
        }
        return phoneNumber; // 전화번호 반환
    }
}
