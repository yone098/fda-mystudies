INSERT INTO questionnaires_steps (step_id, questionnaires_id, instruction_form_id, step_short_title, step_type, sequence_no, destination_step, repeatable, skiappable, active, status) VALUES (1, 11675, 85231, '1time', 'Question', 1, 60873, 'No', 'Yes', 1, 1);

INSERT INTO questionnaires (id, study_id, frequency, title, study_lifetime_start, study_lifetime_end, short_title, repeat_questionnaire, created_by, created_date, modified_by, modified_date, branching, active, status, custom_study_id, is_live, version, is_Change, schedule_type) VALUES (1, 1433, 'One time', 'quesions', '2020-09-02', '2020-09-23', 'onetime', 5, 0, '2020-09-02 13:03:11', 0, '2020-09-02 14:25:43', 1, 1, 1, 'OpenStudy003', 1, 1, 1, 'Regular');

INSERT INTO study_version (version_id, activity_version, custom_study_id, study_version, consent_version) VALUES (1, 1, 'OpenStudy003', 1, 1);

INSERT INTO form_mapping (id, form_id, question_id, sequence_no) VALUES (1, 58, 85199, 1);

INSERT INTO `questions` (`id`, `active`, `add_line_chart`, `description`, `modified_by`, `response_type`, `status`, `use_anchor_date`, `use_stastic_data`, `allow_healthkit`) VALUES ('1', '1', 'No', 'hi', '1', '6', '1', '0', 'No', 'No');

INSERT INTO `resources` (`id`, `study_id`, `title`, `text_or_pdf`, `rich_text`, `pdf_url`, `resource_visibility`, `resource_text`, `action`, `study_protocol`, `created_by`, `modified_by`, `status`, `pdf_name`, `sequence_no`) VALUES ('1', '1374', 'resource', '1', 'a@gmail.com', 'abcd.pdf', '1', 'text2', '1', '0', '1', '1', '1', 'ab.pdf', '1');