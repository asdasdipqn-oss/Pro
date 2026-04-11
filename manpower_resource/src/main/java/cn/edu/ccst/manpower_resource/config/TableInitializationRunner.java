package cn.edu.ccst.manpower_resource.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库表初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableInitializationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        initCandidateProfileHistoryTable();
    }

    private void initCandidateProfileHistoryTable() {
        try {
            // 检查表是否存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'candidate_profile_history'",
                Integer.class
            );

            if (count != null && count > 0) {
                log.info("Table candidate_profile_history already exists, skipping initialization");
                return;
            }

            log.info("Creating table candidate_profile_history...");
            String sql = """
                CREATE TABLE `candidate_profile_history` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
                  `candidate_id` BIGINT NOT NULL COMMENT '求职者ID',
                  `real_name` VARCHAR(100) COMMENT '真实姓名',
                  `phone` VARCHAR(20) COMMENT '手机号码',
                  `email` VARCHAR(100) COMMENT '邮箱',
                  `gender` INT COMMENT '性别：0-女 1-男',
                  `id_card` VARCHAR(20) COMMENT '身份证号',
                  `education` INT COMMENT '学历：1-高中 2-大专 3-本科 4-硕士 5-博士',
                  `graduate_school` VARCHAR(100) COMMENT '毕业院校',
                  `major` VARCHAR(100) COMMENT '专业',
                  `graduate_date` VARCHAR(20) COMMENT '毕业日期',
                  `work_experience` INT COMMENT '工作年限（年）',
                  `expected_salary` DECIMAL(10,2) COMMENT '期望薪资',
                  `expected_position` VARCHAR(100) COMMENT '期望岗位',
                  `resume_url` VARCHAR(500) COMMENT '简历地址',
                  `submit_time` DATETIME COMMENT '提交时间',
                  `create_time` DATETIME NOT NULL COMMENT '创建时间',
                  `update_time` DATETIME COMMENT '更新时间',
                  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                  PRIMARY KEY (`id`),
                  KEY `idx_candidate` (`candidate_id`),
                  KEY `idx_submit_time` (`submit_time`),
                  KEY `idx_deleted` (`deleted`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求职者个人信息修改历史表'
                """;

            jdbcTemplate.execute(sql);
            log.info("Table candidate_profile_history created successfully");
        } catch (Exception e) {
            log.error("Failed to initialize candidate_profile_history table", e);
        }
    }
}
