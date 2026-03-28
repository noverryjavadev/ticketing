# APLIKASI PEMESANAN TIKET BUS

### Dibangun menggunakan Java Springboot

#### List API 
    
    Auth:
    * Register : /api/v1/auth/register
    * Login : /api/v1/auth/authenticate
    * Refresh-Token : /api/v1/auth/refresh-token

````
src/main/java/com/bus/ticket/
├── controller/
│   ├── AuthController.java (sudah ada)
│   ├── BusController.java (sudah ada)
│   ├── OrderController.java (baru)
│   └── QueueController.java (baru - untuk manajemen antrian)
├── service/
│   ├── OrderService.java (baru)
│   ├── QueueService.java (baru)
│   └── SeatLockService.java (baru)
├── repository/
│   ├── OrderRepository.java (baru)
│   ├── SeatRepository.java (baru)
│   └── ScheduleRepository.java (baru)
├── model/
│   ├── Order.java (baru)
│   ├── Seat.java (baru)
│   ├── Schedule.java (baru)
│   └── enums/
│       ├── OrderStatus.java (baru)
│       └── SeatStatus.java (baru)
├── dto/
│   ├── OrderRequest.java (baru)
│   ├── OrderResponse.java (baru)
│   ├── QueueStatusResponse.java (baru)
│   └── BookingResult.java (baru)
└── config/
├── RedisConfig.java (baru)
└── AsyncConfig.java (baru)