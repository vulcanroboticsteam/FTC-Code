/*
 * Copyright (c) 2019 Vulcan Robotics FTC Team 8375. Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.firstinspires.ftc.team8375.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.team8375.Subsystems.Robot;
import org.firstinspires.ftc.team8375.Subsystems.VulcanPID;
import org.firstinspires.ftc.team8375.Subsystems.VulcanPIDCoefficients;

enum driveType {
    MECANUM, TANK
}

public abstract class VulcanPipeline extends LinearOpMode {
    private double speed = 0;
    private double pidOut;
    private double integral = 0;
    private double derivative = 0;
    private double previousError = 0;
    protected int step = 0;
    private int i;
    protected Robot robot;
    private ElapsedTime stoneTime = new ElapsedTime();
    protected ElapsedTime armTime = new ElapsedTime();
    private VulcanPIDCoefficients turnCoefficients = new VulcanPIDCoefficients(0.5, 0.6, 1);
    private VulcanPIDCoefficients moveCoefficients = new VulcanPIDCoefficients(-1, -1, -1, 5);
    private BNO055IMU imu;
    private VulcanPID movePid;
    private VulcanPID turnPid;

    protected boolean isDone = false;
    private boolean async;

    private driveType driveMode;

    public void initialize() {
        robot = new Robot(hardwareMap);
        robot.drivetrain.init();
        robot.drivetrain.resetEncoders(DcMotor.RunMode.RUN_USING_ENCODER);
        imu = robot.drivetrain.imu;
        movePid = new VulcanPID(imu);
        turnPid = new VulcanPID(imu);
        movePid.init();
        turnPid.init();
        isDone = false;
    }

    public abstract void runOpMode();

    //turn
//    public void turn(double angle, double speed) {
//        this.speed = speed;
//        do {
//            if (Math.abs(robot.drivetrain.getError()) <= 20) {
//                if (this.speed > 0) {
//                    this.speed--;
//                    this.speed = Range.clip(this.speed, 5, 100);
//                } else if (speed < 0) {
//                    this.speed++;
//                    this.speed = Range.clip(this.speed, -100, -5);
//                }
//
//                sleep(7);
//            }
//            robot.drivetrain.turn(angle, this.speed);
//            sleep(10);
//            telemetry.addData("imu", robot.drivetrain.getImuAngle());
//            updateTelemetry();
//
//        } while (!robot.drivetrain.isTurnDone());
//        robot.drivetrain.setPowers(0, 0);
//        robot.drivetrain.setImuOffset(robot.drivetrain.getImuAngle());
//        step++;
//    }

    //moveIn
    public void moveIn(double inches, double speed, double turn) {
        robot.drivetrain.moveIn(inches, speed, turn);
        step++;
        updateTelemetry();
    }
    public void moveIn(double inches, double speed) {
        moveIn(inches, speed, 0);

    }

    public void move(double inches, double speed) {

        double wheelSize = (100.0/25.4) * Math.PI;
        int targetPos = (int) Math.round((inches/wheelSize) * 537.6);
        while(robot.drivetrain.getPosition() != targetPos) {
            double inchesTravelled = (robot.drivetrain.getPosition() / 537.6) * wheelSize;
            movePid.run(inches, inchesTravelled, moveCoefficients);
            robot.drivetrain.movePercent(speed, robot.drivetrain.pid.getOutput());
            telemetry.addData("pos", robot.drivetrain.getPosition());
            telemetry.addData("output", robot.drivetrain.pid.getOutput());
            telemetry.update();
            step++;
            if(async) {
                async();
            }
        }
        robot.drivetrain.setPowers(0, 0);
    }

    private void pid(double Kp, double Ki, double Kd, long iterationTime, double heading) {
        double sensorVal = robot.drivetrain.pid.getIntegratedHeading() + robot.drivetrain.pid.getStartHeading();

        double error = sensorVal - heading;
        integral += ((error + previousError) / 2.0) * (iterationTime / 1000.0);
        integral = Range.clip(integral, -100, 100);
        derivative = (error - previousError);
        pidOut = Kp * error + Ki * integral + Kd * derivative;
        previousError = error;

        if(Math.abs(error) < 10) {
            pidOut = Range.clip(pidOut, -40, 40);
        } else {
            pidOut = Range.clip(pidOut, -100, 100);
        }
        sleep(iterationTime);
        updateTelemetry();
    }

    public void turn(double heading, double speed) {
        robot.drivetrain.pid.initHeading();

        while(Math.ceil(robot.drivetrain.getImuAngle()) != heading) {
            pid(0.5, 0.6, 1, 7, heading);
            robot.drivetrain.turnPercent(speed, pidOut);
            step++;
            if(async) {
                async();
            }
        }
        robot.drivetrain.setPowers(0, 0);
        step++;

    }

    public void findSkystone(double threshold, double power) {
        stoneTime.reset();
        robot.SkystoneDetect.resetScore();

        while (!robot.SkystoneDetect.detect()) {
//            robot.SkystoneDetect.setScorerThreshold(threshold);
            robot.drivetrain.setPowers(power, 0);
            robot.SkystoneDetect.resetScore();
            sleep(100);
            telemetry.addData("score", robot.SkystoneDetect.getScorer());
            if(async) {
                async();
            }

        }
        robot.drivetrain.setPowers(0, 0);
        step++;

        if(stoneTime.milliseconds() <= 1000) {
            i = 1;
        } else if(stoneTime.milliseconds() >= 1000 && stoneTime.milliseconds() < 2000) {
            i = 2;
        } else if(stoneTime.milliseconds() > 2000) {
            i = 3;
        }
    }

    public void deployAutoArm() {
        robot.autoArm.setFlipPos(135);
        robot.autoArm.setClawPos(170);
        robot.autoArm.setLiftPower(1);
        sleep(3200);
        robot.autoArm.setClawPos(90);
        robot.autoArm.setLiftTime(-1, 1600);
        robot.autoArm.setFlipPos(45);
        robot.autoArm.setLiftPower(0);
    }

    public void releaseAutoArm() {
        robot.autoArm.setFlipPos(135);
        robot.autoArm.setLiftTime(1, 1600);
        robot.autoArm.setClawPos(90);
    }

    public void retractAutoArm() {
        robot.autoArm.setLiftTime(-1, 1600);
        robot.autoArm.setFlipPos(45);
        robot.autoArm.setClawPos(90);
    }


    public abstract void async();

    public void sleepOpMode(long millis) {
        sleep(millis);
    }

    public int returnInt() {
        return i;
    }

    public void updateTelemetry() {

        telemetry.addData("step", step);

        telemetry.addData("Runtime", getRuntime());

        telemetry.update();

    }

    public driveType getDriveMode() {
        return driveMode;
    }

    public void setDriveMode(driveType mode) {

        driveMode = mode;

    }
}
