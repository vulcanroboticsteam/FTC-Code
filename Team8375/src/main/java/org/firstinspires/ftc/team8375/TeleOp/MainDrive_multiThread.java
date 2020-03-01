/*
 * Copyright (c) 2020 Vulcan Robotics FTC Team 8375. Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.firstinspires.ftc.team8375.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.team8375.Robot.FullBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainDrive_multiThread extends LinearOpMode {

    private FullBot robot;
    private boolean buttonPressed;
    private int inverse = 1;
    private double trigger;
    private Properties prop;

    public void runOpMode() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream("config.properties");
            if(input != null) {
                telemetry.addLine("inputstream loaded");
                prop = new Properties();
                prop.load(input);
                telemetry.update();
            } else {

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        robot = new FullBot(hardwareMap);
        telemetry.addLine("robot loaded");
        robot.drivetrain.init();
        telemetry.update();
        Thread telemetryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(opModeIsActive()) {

                    //telemetry here
                    telemetry.addData("front Left", robot.drivetrain.getPositionFl());
                    telemetry.addData("front Right", robot.drivetrain.getPositionFr());
                    telemetry.addData("back Left", robot.drivetrain.getPositionBl());
                    telemetry.addData("back Right", robot.drivetrain.getPositionBr());

                    //Arm
                    telemetry.addData("liftLeft", robot.arm.getLiftLeftPos());
                    telemetry.addData("liftRight", robot.arm.getLiftRightPos());

                    telemetry.addData("ir sensor", robot.intake.getIRDistance(DistanceUnit.CM));

                    telemetry.addData("dataStream test", prop.getProperty("arm.theta"));
                    telemetry.addData("Runtime", getRuntime());

                    telemetry.update();

                    if(isStopRequested())
                        return;
                }
            }
        });
        //init code

        waitForStart();
        while(opModeIsActive()) {
            telemetryThread.start();

            robot.drivetrain.tankDrive(
                    //forward
                    -gamepad1.left_stick_y,
                    //turn
                    -gamepad1.right_stick_x,
                    //slow mode
                    gamepad1.left_bumper
            );

            robot.arm.run(
                    //lift
                    -gamepad2.left_stick_y,
                    //extend
                    gamepad2.right_stick_y,
                    //adjust
                    trigger,
                    //half flip
                    gamepad2.x,
                    //claw
                    gamepad2.left_bumper,
                    //up button
                    false,
                    //reset
                    gamepad2.y
            );

            robot.intake.run(
                    //reverse
                    gamepad2.a,
                    //toggle
                    intakeToggle()
            );

            if(gamepad2.dpad_down) {
                robot.foundation.setFoundationMoveAngle(Double.parseDouble(prop.getProperty("foundation.deployed")));
            } else {
                robot.foundation.setFoundationMoveAngle(Double.parseDouble(prop.getProperty("foundation.retracted")));
            }

            robot.foundation.deployCapstone(gamepad2.dpad_left);

//        if(gamepad1.left_bumper && !buttonPressed) {
//            inverse *= -1;
//            buttonPressed = true;
//        }
//        else if(buttonPressed && !gamepad1.left_bumper) {
//            buttonPressed = false;
//        }

            if(inverse > 0) {
                robot.autoArm.setClawPos(0);
            } else if(inverse < 0) {
                robot.autoArm.setClawPos(90);
            }

            if(isStopRequested())
                return;
        }

        try {
            telemetryThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private double intakeToggle() {
        if(gamepad2.right_bumper)
            return 1;
        else
            return 0;
    }

}