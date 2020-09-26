INSERT INTO questionnaires_steps (step_id, questionnaires_id, instruction_form_id, step_short_title, step_type, sequence_no, destination_step, repeatable, skiappable, active, status) VALUES (1, 11675, 85231, '1time', 'Question', 1, 60873, 'No', 'Yes', 1, 1), (2, 1, 1, '1time', 'Question', 1, 60873, 'No', 'Yes', 1, 1);

INSERT INTO questionnaires (id, study_id, frequency, title, study_lifetime_start, study_lifetime_end, short_title, repeat_questionnaire, created_by, created_date, modified_by, modified_date, branching, active, status, custom_study_id, is_live, version, is_Change, schedule_type) VALUES (1, 58, 'One time', 'quesions', '2020-09-02', '2020-09-23', 'onetime', 5, 0, '2020-09-02 13:03:11', 0, '2020-09-02 14:25:43', 1, 1, 1, 'OpenStudy003', 1, 1, 1, 'Regular'), (2, 58, 'One time', 'quesions', '2020-09-02', '2020-09-23', 'onetime', 5, 0, '2020-09-02 13:03:11', 0, '2020-09-02 14:25:43', 1, 1, 1, 'OpenStudy003', 1, 1, 1, 'Regular');

INSERT INTO study_version (version_id, activity_version, custom_study_id, study_version, consent_version) VALUES (1, 1, 'OpenStudy003', 1, 1);

INSERT INTO form_mapping (id, form_id, question_id, sequence_no) VALUES (1, 58, 85199, 1);

INSERT INTO `questions` (`id`, `active`, `add_line_chart`, `description`, `modified_by`, `response_type`, `status`, `use_anchor_date`, `use_stastic_data`, `allow_healthkit`) VALUES ('1', '1', 'No', 'hi', '1', '6', '1', '0', 'No', 'No');

INSERT INTO `resources` (`id`, `study_id`, `title`, `text_or_pdf`, `rich_text`, `pdf_url`, `resource_visibility`, `resource_text`, `action`, `study_protocol`, `created_by`, `modified_by`, `status`, `pdf_name`, `sequence_no`) VALUES ('1', '1374', 'resource', '1', 'a@gmail.com', 'abcd.pdf', '1', 'text2', '1', '0', '1', '1', '1', 'ab.pdf', '1');

INSERT INTO `users` (`user_id`, `first_name`, `last_name`, `email`, `password`, `role_id`, `created_by`, `modified_by`, `status`, `accountNonExpired`, `accountNonLocked`, `credentialsNonExpired`, `force_logout`) VALUES ('1', 'abc', 'xyx', 'ttt@gmail.com', '$2a$10$uSKnFqkar9ugqrdD1KElcOcPGEtdpuNMvwlHfRGwX4jovq.sH0e/q', '1', '1', '1', '1', '1', '1', '1', 'N');
		
INSERT INTO `study_sequence` (`study_sequence_id`, `study_id`, `actions`, `basic_info`, `check_list`, `comprehension_test`, `consent_edu_info`, `e_consent`, `eligibility`, `miscellaneous_branding`, `miscellaneous_notification`, `miscellaneous_resources`, `over_view`, `setting_admins`, `study_dashboard_chart`, `study_dashboard_stats`, `study_exc_active_task`, `study_exc_questionnaries`) VALUES (10633, 678574, 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N');

INSERT INTO `active_task` (`id`, `study_id`, `frequency`, `duration`, `active_task_lifetime_start`, `active_task_lifetime_end`, `day_of_the_week`, `repeat_active_task`, `created_by`, `modified_by`, `display_name`, `instruction`, `short_title`, `created_date`, `modified_date`, `task_title`, `task_type_id`, `action`, `custom_study_id`, `is_live`, `is_Change`, `active`, `anchor_date_id`, `schedule_type`) VALUES (28500, 678574, 'One time', NULL, '2020-03-24', NULL, NULL, NULL, 59, 59, 'FetalKickCounter', 'FetalKickCounter', 'FetalKickCounter', '2020-03-24 08:04:34', '2020-03-24 08:04:48', NULL, 1, 1, 678590, 1, 0, 1, NULL, 'Regular');

INSERT INTO `active_task_attrtibutes_values` (`active_task_attribute_id`, `active_task_id`, `active_task_master_attr_id`, `attribute_val`, `add_to_line_chart`, `time_range_chart`, `rollback_chat`, `title_chat`, `use_for_statistic`, `identifier_name_stat`, `display_name_stat`, `display_units_stat`, `upload_type_stat`, `formula_applied_stat`, `time_range_stat`) VALUES (7576, 28500, 1, '23:59', 'N', NULL, NULL, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `active_task_frequencies` (`id`, `active_task_id`, `frequency_date`, `frequency_time`, `is_launch_study`, `is_study_life_time`, `study_version`, `time_period_from_days`, `time_period_to_days`, `x_days_sign`, `y_days_sign`) VALUES (28500, 2850, NULL, NULL, 1, 1, NULL, NULL, NULL, b'1', b'1');

INSERT INTO `active_task_custom_frequencies` (`id`, `frequency_start_date`, `frequency_end_date`, `frequency_time`, `active_task_id`, `study_version`, `is_used`, `time_period_from_days`, `time_period_to_days`, `x_days_sign`, `y_days_sign`) VALUES (28500, '2020-03-24', '2020-03-30', '22:07:00', 2862, NULL, 'N', NULL, NULL, b'0', b'0');

INSERT INTO `study_version` (`version_id`, `activity_version`, `custom_study_id`, `study_version`, `consent_version`) VALUES (1979, 1, 67859, 1, 1);