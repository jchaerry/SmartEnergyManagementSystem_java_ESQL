package com.ESQL.SmartEnergy.HLL;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.ESQL.SmartEnergy.HLL.UserService.userId;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();// 서비스 클래스 인스턴스 생성
        DeviceService deviceService = new DeviceService();
        EnergyUsageService energyUsageService = new EnergyUsageService();
        AlertService alertService = new AlertService();
        CrossCheckService crossCheckService = new CrossCheckService();

        userService.createUserTable();
        deviceService.createDeviceTable();
        energyUsageService.createEnergyUsageTable();
        alertService.createAlertTable();
        userId = userId;

        while (true) {
                if (userId == 0) {
                    System.out.println("\n===== User Management Console =====");
                    System.out.println("0. Cross Check");
                    System.out.println("1. 회원 가입");
                    System.out.println("2. 로그인");
                    System.out.println("3. 회원 정보 찾기");
                    System.out.println("4. 회원 정보 삭제");
                    System.out.println("5. 종료");
                    System.out.print("옵션 선택하기: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 0:
                            crossCheckService.choiceCrossCheck(scanner);
                            break;
                        case 1:
                            userService.registerUser(scanner);
                            break;
                        case 2:
                            userService.loginUser(scanner);
                            alertService.checkAndGenerateAlerts(userId);
                            break;
                        case 3:
                            userService.findUser(scanner);
                            break;
                        case 4:
                            userService.deleteUser(scanner);
                            break;
                        case 5:
                            System.out.println("프로그램을 종료합니다.");
                            return;
                        default:
                            System.out.println("잘못된 옵션입니다. 다시 시도하세요.");
                            break;
                    }
                } else {
                    System.out.println("\n===== Logged In Menu =====");
                    System.out.println("현재 로그인된 회원 전화번호: " + userService.getPhoneNumberByUserId(userId));
                    System.out.println("안 읽은 알람");
                    alertService.displayAlerts(userId);
                    System.out.println("--------------------------------------");
                    System.out.println("0. Cross Check");
                    System.out.println("1. 로그아웃(유저 관리로 돌아갑니다.)");
                    System.out.println("2. 모든 기기 불러오기");
                    System.out.println("3. 새 기기 등록하기");
                    System.out.println("4. 기기 에너지 사용 기록 등록하기");
                    System.out.println("5. 한 달 에너지 사용량 조회");
                    System.out.println("6. 알람 읽음 처리");
                    System.out.println("7. 프로그램 종료");
                    System.out.print("옵션 선택하기: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 0:
                            crossCheckService.choiceCrossCheck(scanner);
                            break;
                        case 1:
                            userId = 0;
                            System.out.println("로그아웃 되었습니다.");
                            break;
                        case 2:
                            List<Map<String, Object>> devices = deviceService.findAllDevice(userId);
                            if (devices.isEmpty()) {
                                System.out.println("등록된 기기가 없습니다.");
                            } else {
                                // 각 기기 정보를 한 줄씩 출력
                                for (Map<String, Object> device : devices) {
                                    System.out.println();
                                    System.out.println("기기 고유 번호: " + device.get("기기 번호"));
                                    System.out.println("기기 유형: " + device.get("기기 유형"));
                                    System.out.println("에너지 등급: " + device.get("에너지 등급"));
                                    System.out.println("----------------------------");
                                }
                            }
                            break;
                        case 3:
                            deviceService.registerDevice(scanner, userId);
                            alertService.checkAndGenerateAlerts(userId);
                            break;
                        case 4:
                            devices = deviceService.findAllDevice(userId);
                            if (devices.isEmpty()) {
                                System.out.println("등록된 기기가 없습니다.");
                            } else {
                                // 각 기기 정보를 한 줄씩 출력
                                for (Map<String, Object> device : devices) {
                                    System.out.println();
                                    System.out.println("기기 고유 번호: " + device.get("기기 번호"));
                                    System.out.println("기기 유형: " + device.get("기기 유형"));
                                    System.out.println("에너지 등급: " + device.get("에너지 등급"));
                                    System.out.println("----------------------------");
                                }
                            }
                            energyUsageService.addEnergyUsage(scanner, userId);
                            alertService.checkAndGenerateAlerts(userId);
                            break;
                        case 5:
                            energyUsageService.calculateMonthlyUsage(userId);
                            break;
                        case 6:
                            alertService.resolveAlert(scanner, userId);
                            break;
                        case 7:
                            System.out.println("프로그램을 종료합니다.");
                            return;
                        default:
                            System.out.println("잘못된 옵션입니다. 다시 시도하세요.");
                            break;
                    }
                }
        }
    }
}
