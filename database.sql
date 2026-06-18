

-- USERS TABLE


CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role ENUM('student','teacher','principal','librarian') NOT NULL
);

-- TIMETABLE TABLE

CREATE TABLE timetable (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    day_of_week VARCHAR(20),
    period_time TIME,
    subject VARCHAR(100),
    room VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- AUDIT LOG TABLE

CREATE TABLE audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255)
);

 
-- SAMPLE USERS


INSERT INTO users(username,password,role)
VALUES
('student1','pass123','student'),
('teacher1','teach123','teacher'),
('principal1','admin123','principal'),
('librarian1','lib123','librarian');


-- TRIGGER


DELIMITER $$

CREATE TRIGGER schedule_insert_trigger
AFTER INSERT ON timetable
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(description)
    VALUES(
        CONCAT('Schedule Added For User ID ', NEW.user_id)
    );
END$$

DELIMITER ;

-- STORED PROCEDURE:

DELIMITER $$

CREATE PROCEDURE AddSchedule(
    IN p_user_id INT,
    IN p_day VARCHAR(20),
    IN p_time TIME,
    IN p_subject VARCHAR(100),
    IN p_room VARCHAR(50)
)
BEGIN
    INSERT INTO timetable(
        user_id,
        day_of_week,
        period_time,
        subject,
        room
    )
    VALUES(
        p_user_id,
        p_day,
        p_time,
        p_subject,
        p_room
    );
END$$

DELIMITER ;

-- STORED PROCEDURE: ValidateUser

DELIMITER $$

CREATE PROCEDURE ValidateUser(
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(50)
)
BEGIN
    SELECT user_id, role
    FROM users
    WHERE username = p_username
    AND password = p_password;
END$$

DELIMITER ;


-- STORED PROCEDURE: GetScheduleByUser

DELIMITER $$

CREATE PROCEDURE GetScheduleByUser(
    IN p_user_id INT
)
BEGIN
    SELECT *
    FROM timetable
    WHERE user_id = p_user_id
    ORDER BY day_of_week, period_time;
END$$

DELIMITER ;

-- STORED PROCEDURE: GetAllSchedules


DELIMITER $$

CREATE PROCEDURE GetAllSchedules()
BEGIN
    SELECT *
    FROM timetable
    ORDER BY user_id, day_of_week, period_time;
END$$

DELIMITER ;


-- MYSQL USERS

CREATE USER IF NOT EXISTS 'student_user'@'localhost'
IDENTIFIED BY 'student123';

CREATE USER IF NOT EXISTS 'teacher_user'@'localhost'
IDENTIFIED BY 'teacher123';

CREATE USER IF NOT EXISTS 'principal_user'@'localhost'
IDENTIFIED BY 'principal123';


-- GRANT PRIVILEGES

GRANT SELECT
ON hemalatha.timetable
TO 'student_user'@'localhost';

GRANT SELECT
ON hemalatha.timetable
TO 'teacher_user'@'localhost';

GRANT ALL PRIVILEGES
ON hemalatha.*
TO 'principal_user'@'localhost';

FLUSH PRIVILEGES;


-- REVOKE EXAMPLE


REVOKE INSERT
ON hemalatha.timetable
FROM 'student_user'@'localhost';

-- STUDENT TIMETABLE (USER_ID = 1)

CALL AddSchedule(1,'Monday','09:00:00','DBMS','2nd CSE');
CALL AddSchedule(1,'Monday','10:00:00','Data Structures','2nd CSE');
CALL AddSchedule(1,'Monday','11:00:00','Maths','2nd CSE');
CALL AddSchedule(1,'Monday','12:00:00','Java Programming','2nd CSE');


-- TEACHER TIMETABLE (USER_ID = 2)

CALL AddSchedule(2,'Monday','09:00:00','DBMS','2nd CSE');
CALL AddSchedule(2,'Monday','10:00:00','OOPs','3rd CSE');
CALL AddSchedule(2,'Monday','11:00:00','Operating Systems','2nd IT');

-- TEST PROCEDURES

CALL ValidateUser('student1','pass123');
CALL ValidateUser('teacher1','teach123');

CALL GetScheduleByUser(1);
CALL GetScheduleByUser(2);

CALL GetAllSchedules();

-- VIEW DATA

SELECT * FROM users;
SELECT * FROM timetable;
SELECT * FROM audit_log;