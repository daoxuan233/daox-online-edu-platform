/*
 Navicat Premium Dump SQL

 Source Server         : MySQL-Windows
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : online_educate

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 01/04/2026 15:06:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for assessments
-- ----------------------------
DROP TABLE IF EXISTS `assessments`;
CREATE TABLE `assessments`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测评ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `creator_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建者ID',
  `assessment_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'homework' COMMENT '测评类型(ClassroomExam [课堂作业]、ChapterExam [章节作业]、MidtermExam [期中考试]、FinalExam [期末考试]、homework [作业])',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测评标题',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration_minutes` int NOT NULL COMMENT '答题时长(分)',
  `is_published` tinyint NULL DEFAULT 0 COMMENT '是否发布 0-未发布，1-发布，2过期[逻辑删除]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  INDEX `creator_id`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `assessments_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `assessments_ibfk_2` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测评活动表\r\n由教师发起的测评活动（如期中考试、课后作业、平时检查等）。\r\n规定了测评的名称、时间范围和时长等基本信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operator_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作者用户ID',
  `operator_identifier` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operator_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operator_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_source` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `before_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `after_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_logs_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_audit_logs_resource`(`resource_type` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_audit_logs_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chapters
-- ----------------------------
DROP TABLE IF EXISTS `chapters`;
CREATE TABLE `chapters`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节标题',
  `order_index` int NULL DEFAULT 0 COMMENT '显示顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '章节表\r\n将一门复杂的课程分解为多个逻辑单元（章节）。\r\n它作为课程内容的组织层级，每个章节下可包含若干个具体的小节。' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_categories
-- ----------------------------
DROP TABLE IF EXISTS `course_categories`;
CREATE TABLE `course_categories`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '父分类ID (0/null表示顶级分类)',
  `order_index` int NULL DEFAULT 0 COMMENT '排序值，值越小越靠前',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程分类表\r\n用于对课程进行结构化分类，方便用户按类别浏览和检索课程。\r\n支持父子关系\r\n可以构建多层级的课程目录体系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_instructor
-- ----------------------------
DROP TABLE IF EXISTS `course_instructor`;
CREATE TABLE `course_instructor`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '讲师ID',
  `teacher_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '讲师职位/头衔',
  `biography` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '讲师简介',
  PRIMARY KEY (`id` DESC) USING BTREE,
  INDEX `teacher_id`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `course_instructor_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程讲师信息表\r\n辅助讲师身份说明' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_materials
-- ----------------------------
DROP TABLE IF EXISTS `course_materials`;
CREATE TABLE `course_materials`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资料ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `chapter_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '章节ID',
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '小节ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资料标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '资料描述',
  `file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件ID，外键关联files表',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `is_public` tinyint NULL DEFAULT 1 COMMENT '1=公开，0=仅选课学生可见',
  `upload_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标志 [1-删除，0-正常]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  INDEX `chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `section_id`(`section_id` ASC) USING BTREE,
  INDEX `fk_materials_to_files`(`file_id` ASC) USING BTREE,
  CONSTRAINT `course_materials_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `course_materials_ibfk_2` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `course_materials_ibfk_3` FOREIGN KEY (`section_id`) REFERENCES `sections` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_materials_to_files` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程资料表\r\n用于存储课程相关的学习资料文件，支持按课程、章节、小节进行分层管理。\r\n提供完整的文件元数据管理和下载统计功能，是课程资源管理的核心表。' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_properties
-- ----------------------------
DROP TABLE IF EXISTS `course_properties`;
CREATE TABLE `course_properties`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程属性id',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程id',
  `level` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'beginner' COMMENT '课程难度等级 (\'beginner\' [初级], \'intermediate\' [中级], \'advanced\' [高级])',
  `is_new` tinyint NULL DEFAULT 1 COMMENT '是否为新课程 (0表示FALSE，1表示TRUE)',
  `target_audience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适合人群',
  `requirements` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '技术要求',
  `price` decimal(8, 2) NULL DEFAULT NULL COMMENT '当前价格',
  `original_price` decimal(8, 2) NULL DEFAULT NULL COMMENT '原价，用于显示折扣',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `course_properties_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程属性表\r\n说明课程的附属信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_ratings
-- ----------------------------
DROP TABLE IF EXISTS `course_ratings`;
CREATE TABLE `course_ratings`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分用户ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `overall_rating` decimal(2, 1) NOT NULL COMMENT '总体评分 (1.0-5.0)',
  `content_quality` decimal(2, 1) NULL DEFAULT NULL COMMENT '内容质量评分 (1.0-5.0)',
  `difficulty_level` decimal(2, 1) NULL DEFAULT NULL COMMENT '难度适中性评分 (1.0-5.0)',
  `practicality` decimal(2, 1) NULL DEFAULT NULL COMMENT '实用性评分 (1.0-5.0)',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字评价',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否匿名评分 (0-实名, 1-匿名)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志 (0-正常, 1-删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE COMMENT '用户对同一课程只能评分一次',
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_overall_rating`(`overall_rating` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `course_ratings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `course_ratings_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程评分表\r\n学生对课程的多维度评分和评价，支持匿名评分' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_review_log
-- ----------------------------
DROP TABLE IF EXISTS `course_review_log`;
CREATE TABLE `course_review_log`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '记录ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `from_status` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变更前状态',
  `to_status` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变更后状态',
  `operator_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人ID',
  `operator_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人角色 teacher/admin',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见（pending→draft 时必填）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_review_log_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程审核流转记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程描述',
  `cover_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `teacher_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师ID',
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类ID',
  `status` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '课程状态: draft[草稿] → pending[待审核] → published[已发布] → taken_down[已下架] → archived[已归档]',
  `enrollment_count` int NULL DEFAULT NULL COMMENT '学习人数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标志 [1-\"删除\"，0-\"正常\"]',
  `is_private` tinyint NULL DEFAULT 0 COMMENT '逻辑标志 [1-\"私有\"，0-\"公开\"]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `category_id`(`category_id` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_description`(`title`, `description`) WITH PARSER `ngram`,
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `courses_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `course_categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表\r\n存储课程的核心信息-是组织教学活动（章节、测评）的中心实体' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for files
-- ----------------------------
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件ID',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `object_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '在存储系统中的对象名称（包含路径）',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型分类（video/image/document/material）',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MIME类型',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `uploader_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件上传者ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件标题（可选）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文件描述（可选）',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `public_access` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开 1=公开，0=私有',
  `allow_download` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许下载 (1=是, 0=否)',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志 [1-删除，0-正常]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uploader_type`(`uploader_id` ASC, `file_type` ASC) USING BTREE,
  INDEX `idx_object_name`(`object_name` ASC) USING BTREE,
  INDEX `idx_upload_time`(`upload_time` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_progress
-- ----------------------------
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '进度ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节ID',
  `status` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'in_progress' COMMENT '学习状态 \'not_started [ 未开始 ]\', \'in_progress [ 进行中 ]\', \'completed [ 已完成 ]\' , \'start_expired [ 已过期 ]\'',
  `progress_seconds` int NULL DEFAULT 0 COMMENT '视频观看时长(秒)',
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `section_id`(`section_id` ASC) USING BTREE,
  CONSTRAINT `learning_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `learning_progress_ibfk_2` FOREIGN KEY (`section_id`) REFERENCES `sections` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习进度表 \r\n精确记录每个学生对每个课程小节的学习状态，是实现“断点续学”和学习数据分析的关键' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rating_dimensions
-- ----------------------------
DROP TABLE IF EXISTS `rating_dimensions`;
CREATE TABLE `rating_dimensions`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维度ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型 (course-课程, teacher-讲师)',
  `dimension_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维度键名 (如: content_quality, teaching_quality)',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '显示名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维度描述',
  `is_required` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否必填 (0-可选, 1-必填)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用 (0-禁用, 1-启用)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_target_dimension`(`target_type` ASC, `dimension_key` ASC) USING BTREE COMMENT '同一目标类型下维度键名唯一',
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评分维度配置表\r\n配置课程和讲师评分的各个维度，支持动态调整评分项目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rating_statistics
-- ----------------------------
DROP TABLE IF EXISTS `rating_statistics`;
CREATE TABLE `rating_statistics`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '统计ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型 (course-课程, teacher-讲师)',
  `target_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标ID (课程ID或讲师ID)',
  `total_ratings` int NOT NULL DEFAULT 0 COMMENT '总评分数量',
  `average_rating` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '平均评分',
  `rating_distribution` json NULL COMMENT '评分分布统计 (JSON格式: {\"1\":count, \"2\":count, ...})',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_target`(`target_type` ASC, `target_id` ASC) USING BTREE COMMENT '每个目标只有一条统计记录',
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_average_rating`(`average_rating` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评分统计表\r\n实时统计课程和讲师的评分数据，用于快速查询和排序' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sections
-- ----------------------------
DROP TABLE IF EXISTS `sections`;
CREATE TABLE `sections`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节ID',
  `chapter_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节标题',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频URL/文件URL',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '视频时长(秒)/如果是文件则为空',
  `order_index` int NULL DEFAULT 0 COMMENT '显示顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `chapter_id`(`chapter_id` ASC) USING BTREE,
  CONSTRAINT `sections_ibfk_1` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小节表\r\n课程内容的最小学习单元，通常对应一个教学视频或一篇文档。学生的学习进度将记录到小节级别。' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_announcements
-- ----------------------------
DROP TABLE IF EXISTS `system_announcements`;
CREATE TABLE `system_announcements`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `creator_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发布者ID',
  `is_active` tinyint NULL DEFAULT NULL COMMENT '是否生效 0-是[生效]，1-否[失效]',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expired_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表\r\n用于管理员发布面向全站用户的系统级公告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for teacher_ratings
-- ----------------------------
DROP TABLE IF EXISTS `teacher_ratings`;
CREATE TABLE `teacher_ratings`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分用户ID',
  `teacher_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '讲师ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联课程ID',
  `overall_rating` decimal(2, 1) NOT NULL COMMENT '总体评分 (1.0-5.0)',
  `teaching_quality` decimal(2, 1) NULL DEFAULT NULL COMMENT '教学质量评分 (1.0-5.0)',
  `interaction` decimal(2, 1) NULL DEFAULT NULL COMMENT '互动性评分 (1.0-5.0)',
  `professionalism` decimal(2, 1) NULL DEFAULT NULL COMMENT '专业性评分 (1.0-5.0)',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字评价',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否匿名评分 (0-实名, 1-匿名)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志 (0-正常, 1-删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_teacher_course`(`user_id` ASC, `teacher_id` ASC, `course_id` ASC) USING BTREE COMMENT '用户对同一讲师在同一课程中只能评分一次',
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_overall_rating`(`overall_rating` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `teacher_ratings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `teacher_ratings_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `teacher_ratings_ibfk_3` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '讲师评分表\r\n学生对讲师的多维度评分和评价，关联具体课程，支持匿名评分' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for teacher_todos
-- ----------------------------
DROP TABLE IF EXISTS `teacher_todos`;
CREATE TABLE `teacher_todos`  (
  `id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '待办事项主键ID',
  `teacher_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属教师ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '待办事项内容',
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'medium' COMMENT '优先级，high/medium/low',
  `completed` tinyint NOT NULL DEFAULT 0 COMMENT '完成状态，0-未完成，1-已完成',
  `due_date` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0-正常，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_todos_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_teacher_todos_due_date`(`due_date` ASC) USING BTREE,
  INDEX `idx_teacher_todos_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师工作台待办事项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_courses
-- ----------------------------
DROP TABLE IF EXISTS `user_courses`;
CREATE TABLE `user_courses`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `enrollment_date` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `user_courses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_courses_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生选课关联表\r\n实现用户与课程的多对多关系-记录了哪个学生参加了哪门课程' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_describe
-- ----------------------------
DROP TABLE IF EXISTS `user_describe`;
CREATE TABLE `user_describe`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户描述id',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'other' COMMENT '性别：男 [man]、女 [female]、其他 [other]',
  `birthday` datetime NULL DEFAULT NULL COMMENT '生日',
  `biography` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `user_describe_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户描述信息\r\n主要存储：用户性别、手机号、生日、简介等附加信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `user_notifications`;
CREATE TABLE `user_notifications`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '通知ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '接收用户ID',
  `notification_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '通知类型 system_announcement/assessment_published/grading_completed/course_changed',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '来源类型 announcement/assessment/course',
  `source_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '来源业务ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '通知正文',
  `level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'info' COMMENT '通知等级 info/success/important/warning',
  `actor_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '触发人ID',
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '关联课程ID',
  `related_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT '关联业务ID，例如测评ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0正常 1删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime NULL DEFAULT NULL COMMENT '已读时间',
  `expires_at` datetime NULL DEFAULT NULL COMMENT '失效时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_notifications_user_read`(`user_id` ASC, `is_read` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_user_notifications_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_user_notifications_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '统一站内通知中心' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_relationships
-- ----------------------------
DROP TABLE IF EXISTS `user_relationships`;
CREATE TABLE `user_relationships`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关系ID，主键',
  `user_one_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户1的ID (值较小的用户ID)',
  `user_two_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户2的ID (值较大的用户ID)',
  `status` tinyint NOT NULL COMMENT '关系状态 (\r\n0: PENDING [ 等待确认 ], \r\n1: ACCEPTED [ 双方已是好友 ], \r\n2: DECLINED [ 拒绝 ], \r\n3: BLOCKED [ 拉黑 ]\r\n)',
  `action_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行最后操作的用户ID',
  `remark_one` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'user_one_id对user_two_id的备注',
  `remark_two` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'user_two_id对user_one_id的备注',
  `created_at` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_relationship`(`user_one_id` ASC, `user_two_id` ASC) USING BTREE,
  INDEX `fk_action_user`(`action_user_id` ASC) USING BTREE,
  INDEX `idx_user_one_id_status`(`user_one_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_user_two_id_status`(`user_two_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_action_user` FOREIGN KEY (`action_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_one` FOREIGN KEY (`user_one_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_two` FOREIGN KEY (`user_two_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关系表\r\n用于存储用户之间的关系，如好友、黑名单等' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `identifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号/工号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称/用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '\'student\' \'teacher\' \'admin\'',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '逻辑删除标志 [1-\"删除\"，0-\"正常\"]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表\r\n存储所有系统用户（学生、教师、管理员）的基础信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for v_course_material_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_course_material_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_course_material_statistics` AS select `course_materials`.`course_id` AS `course_id`,count(0) AS `material_count`,sum(`course_materials`.`download_count`) AS `total_downloads`,max(`course_materials`.`upload_time`) AS `latest_upload` from `course_materials` where (`course_materials`.`is_deleted` = 0) group by `course_materials`.`course_id`;

-- ----------------------------
-- View structure for v_course_rating_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_course_rating_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_course_rating_statistics` AS select `c`.`id` AS `course_id`,`c`.`title` AS `course_title`,`c`.`teacher_id` AS `teacher_id`,`u`.`nickname` AS `teacher_name`,coalesce(`rs`.`total_ratings`,0) AS `total_ratings`,coalesce(`rs`.`average_rating`,0) AS `average_rating`,coalesce(avg(`cr`.`content_quality`),0) AS `avg_content_quality`,coalesce(avg(`cr`.`difficulty_level`),0) AS `avg_difficulty_level`,coalesce(avg(`cr`.`practicality`),0) AS `avg_practicality` from (((`courses` `c` left join `users` `u` on((`c`.`teacher_id` = `u`.`id`))) left join `rating_statistics` `rs` on(((`rs`.`target_type` = 'course') and (`rs`.`target_id` = `c`.`id`)))) left join `course_ratings` `cr` on(((`cr`.`course_id` = `c`.`id`) and (`cr`.`is_deleted` = 0)))) where (`c`.`is_deleted` = 0) group by `c`.`id`,`c`.`title`,`c`.`teacher_id`,`u`.`nickname`,`rs`.`total_ratings`,`rs`.`average_rating`;

-- ----------------------------
-- View structure for v_file_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_file_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_file_statistics` AS select `files`.`uploader_id` AS `uploader_id`,`files`.`file_type` AS `file_type`,count(0) AS `file_count`,sum(`files`.`file_size`) AS `total_size`,sum(`files`.`download_count`) AS `total_downloads`,max(`files`.`upload_time`) AS `latest_upload` from `files` where (`files`.`is_deleted` = 0) group by `files`.`uploader_id`,`files`.`file_type`;

-- ----------------------------
-- View structure for v_teacher_rating_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_teacher_rating_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_teacher_rating_statistics` AS select `u`.`id` AS `teacher_id`,`u`.`nickname` AS `teacher_name`,coalesce(`rs`.`total_ratings`,0) AS `total_ratings`,coalesce(`rs`.`average_rating`,0) AS `average_rating`,coalesce(avg(`tr`.`teaching_quality`),0) AS `avg_teaching_quality`,coalesce(avg(`tr`.`interaction`),0) AS `avg_interaction`,coalesce(avg(`tr`.`professionalism`),0) AS `avg_professionalism`,count(distinct `tr`.`course_id`) AS `courses_taught` from ((`users` `u` left join `rating_statistics` `rs` on(((`rs`.`target_type` = 'teacher') and (`rs`.`target_id` = `u`.`id`)))) left join `teacher_ratings` `tr` on(((`tr`.`teacher_id` = `u`.`id`) and (`tr`.`is_deleted` = 0)))) where ((`u`.`role` = 'teacher') and (`u`.`is_deleted` = 0)) group by `u`.`id`,`u`.`nickname`,`rs`.`total_ratings`,`rs`.`average_rating`;

-- ----------------------------
-- Triggers structure for table course_ratings
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_course_rating_insert`;
delimiter ;;
CREATE TRIGGER `tr_course_rating_insert` AFTER INSERT ON `course_ratings` FOR EACH ROW BEGIN
    INSERT INTO `rating_statistics` (`id`, `target_type`, `target_id`, `total_ratings`, `average_rating`)
    VALUES (CONCAT('stat_course_', NEW.course_id), 'course', NEW.course_id, 1, NEW.overall_rating)
    ON DUPLICATE KEY UPDATE
        `total_ratings` = `total_ratings` + 1,
        `average_rating` = (
            SELECT AVG(overall_rating) 
            FROM course_ratings 
            WHERE course_id = NEW.course_id AND is_deleted = 0
        );
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table course_ratings
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_course_rating_update`;
delimiter ;;
CREATE TRIGGER `tr_course_rating_update` AFTER UPDATE ON `course_ratings` FOR EACH ROW BEGIN
    IF NEW.is_deleted = 0 THEN
        UPDATE `rating_statistics` SET
            `average_rating` = (
                SELECT AVG(overall_rating) 
                FROM course_ratings 
                WHERE course_id = NEW.course_id AND is_deleted = 0
            ),
            `total_ratings` = (
                SELECT COUNT(*) 
                FROM course_ratings 
                WHERE course_id = NEW.course_id AND is_deleted = 0
            )
        WHERE `target_type` = 'course' AND `target_id` = NEW.course_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table teacher_ratings
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_teacher_rating_insert`;
delimiter ;;
CREATE TRIGGER `tr_teacher_rating_insert` AFTER INSERT ON `teacher_ratings` FOR EACH ROW BEGIN
    INSERT INTO `rating_statistics` (`id`, `target_type`, `target_id`, `total_ratings`, `average_rating`)
    VALUES (CONCAT('stat_teacher_', NEW.teacher_id), 'teacher', NEW.teacher_id, 1, NEW.overall_rating)
    ON DUPLICATE KEY UPDATE
        `total_ratings` = `total_ratings` + 1,
        `average_rating` = (
            SELECT AVG(overall_rating) 
            FROM teacher_ratings 
            WHERE teacher_id = NEW.teacher_id AND is_deleted = 0
        );
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table teacher_ratings
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_teacher_rating_update`;
delimiter ;;
CREATE TRIGGER `tr_teacher_rating_update` AFTER UPDATE ON `teacher_ratings` FOR EACH ROW BEGIN
    IF NEW.is_deleted = 0 THEN
        UPDATE `rating_statistics` SET
            `average_rating` = (
                SELECT AVG(overall_rating) 
                FROM teacher_ratings 
                WHERE teacher_id = NEW.teacher_id AND is_deleted = 0
            ),
            `total_ratings` = (
                SELECT COUNT(*) 
                FROM teacher_ratings 
                WHERE teacher_id = NEW.teacher_id AND is_deleted = 0
            )
        WHERE `target_type` = 'teacher' AND `target_id` = NEW.teacher_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_relationships
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_check_user_order_before_insert`;
delimiter ;;
CREATE TRIGGER `trg_check_user_order_before_insert` BEFORE INSERT ON `user_relationships` FOR EACH ROW BEGIN
    IF NEW.user_one_id >= NEW.user_two_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Constraint violation: user_one_id must be less than user_two_id.';
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_relationships
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_check_user_order_before_update`;
delimiter ;;
CREATE TRIGGER `trg_check_user_order_before_update` BEFORE UPDATE ON `user_relationships` FOR EACH ROW BEGIN
    IF NEW.user_one_id >= NEW.user_two_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Constraint violation: user_one_id must be less than user_two_id.';
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
