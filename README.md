# New Batch Job Framework

Core Batch Job Framework với Java 21 và Spring Boot 3.2.2.

## Yêu cầu

- Java 21
- Maven 3.6+
- Oracle Database (hoặc database khác tùy chỉnh)

## Cấu trúc Project

```
new-batch-job/
├── src/main/java/com/yourcompany/batch/
│   ├── BatchJobApp.java                    # Main Application
│   ├── batch/
│   │   ├── AbstractJob.java                # Core abstract class cho Job
│   │   ├── step/
│   │   │   ├── AbstractStepBuilder.java    # Core abstract class cho Step
│   │   │   └── StepFactory.java            # Factory để tạo Step
│   │   ├── tasklet/
│   │   │   └── AbstractTasklet.java        # Core abstract class cho Tasklet
│   │   └── listener/
│   │       ├── AbstractJobExecutionListener.java
│   │       └── JobCompletionListener.java
│   ├── config/
│   │   ├── BatchConfiguration.java         # Batch configuration
│   │   ├── ApplicationProperties.java       # Application properties
│   │   └── Constants.java                  # Constants
│   ├── domain/
│   │   ├── JobLog.java                     # Entity cho Job Log
│   │   ├── JobLogResult.java               # Entity cho Job Result
│   │   └── JobSetting.java                 # Entity cho Job Setting
│   ├── repository/
│   │   ├── JobLogRepository.java
│   │   ├── JobLogResultRepository.java
│   │   └── JobSettingRepository.java
│   └── service/
│       ├── JobSettingService.java
│       └── dto/
│           └── JobSettingDTO.java
└── src/main/resources/
    └── application.yml
```

## Cách sử dụng

### 1. Tạo một Job mới

```java
@Component
public class MyCustomJob extends AbstractJob {

    private static final String JOB_NAME = "MY_CUSTOM_JOB";

    @Autowired
    private StepFactory stepFactory;

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected Job job() {
        return createJobBuilder(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .start(stepFactory.getStep(MyCustomStepBuilder.class))
            .listener(jobCompletionListener)
            .build();
    }

    @Scheduled(cron = "${application.schedule.my-custom-job}")
    @SchedulerLock(name = JOB_NAME, lockAtMostFor = "14m", lockAtLeastFor = "14m")
    public void schedule() {
        runBySchedule();
    }
}
```

### 2. Tạo một Step mới

```java
@Component
public class MyCustomStepBuilder extends AbstractStepBuilder<InputDTO, OutputDTO> {

    @Autowired
    private MyRepository repository;

    @Override
    protected String getStepName() {
        return "MY_CUSTOM_STEP";
    }

    @Override
    protected int countTotalItems() {
        return repository.count();
    }

    @Override
    protected ItemReader<InputDTO> reader() {
        // Implement your reader
        return new JdbcCursorItemReader<>();
    }

    @Override
    protected ItemProcessor<InputDTO, OutputDTO> processor() {
        return item -> {
            // Process item
            return new OutputDTO();
        };
    }

    @Override
    protected ItemWriter<OutputDTO> writer() {
        return items -> {
            // Write items
        };
    }
}
```

### 3. Tạo một Tasklet mới

```java
@Component
public class MyCustomTasklet extends AbstractTasklet {

    @Override
    protected String getName() {
        return "MY_CUSTOM_TASKLET";
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Implement your tasklet logic
        return RepeatStatus.FINISHED;
    }
}
```

## Configuration

Cấu hình trong `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password

application:
  schedule:
    enabled: true
```

## Build và Run

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

## Tính năng

- ✅ Java 21
- ✅ Spring Boot 3.2.2
- ✅ Spring Batch 5.x (tương thích Spring Boot 3.x)
- ✅ Jakarta Persistence API (thay vì javax)
- ✅ Core framework cho batch jobs
- ✅ Job logging và monitoring
- ✅ Job settings management
- ✅ Distributed locking với ShedLock

## Lưu ý

- Project này chỉ chứa core framework, không có các job nghiệp vụ cụ thể
- Cần tạo database tables cho JobLog, JobLogResult, JobSetting
- Cần cấu hình database connection trong application.yml

