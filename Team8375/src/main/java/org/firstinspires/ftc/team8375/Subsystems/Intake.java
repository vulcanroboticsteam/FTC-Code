/*
 * Copyright (c) 2019 Vulcan Robotics FTC Team 8375. Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.firstinspires.ftc.team8375.Subsystems;

import android.content.Context;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.team8375.dataParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldCanBeLocal")
public class Intake {

    private Context context;
    private ElapsedTime time = new ElapsedTime();
    private ElapsedTime intakeTime = new ElapsedTime();
    private boolean onPressed;
    private int intakeOn = 1;
    private boolean reset = false;
    //motors
    private DcMotor intake_left;
    private DcMotor intake_right;
    private double intakePower;

    //servos
    private CRServo deploy_left;
    private CRServo deploy_right;

    private Properties prop;

    private Rev2mDistanceSensor irSensor;

    public Intake(DcMotor intakeLeft, DcMotor intakeRight, CRServo deployLeft, CRServo deployRight, Rev2mDistanceSensor irSensor) {
        intake_left = intakeLeft;
        intake_right = intakeRight;

        deploy_left = deployLeft;
        deploy_right = deployRight;

        this.irSensor = irSensor;

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream("config.properties");
            if(input != null) {
                prop = new Properties();
                prop.load(input);
            } else {

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }



    public void resetDeployTime() {
        time.reset();
    }

    public void deploy(boolean left, boolean right) {
        if(left) {
            deploy_right.setPower(-1.0);
            deploy_left.setPower(1.0);
        } else if(right) {
            deploy_right.setPower(1.0);
            deploy_left.setPower(-1.0);
        } else {
            deploy_right.setPower(0.05);
            deploy_left.setPower(0);
        }

    }

    public void run(boolean reverse, double isOn) {
        intakePower = dataParser.parseDouble(prop, "intake.power");
        if(isOn > 0) {
            intakeOn = 1;
        } else if(isOn == 0) {
            intakeOn = -1;
        }

        if(intakeOn > 0) {

            if (reverse) {
                intake_left.setPower(-intakePower);
                intake_right.setPower(intakePower);

            } else {

                if(getIRDistance(DistanceUnit.CM) < dataParser.parseDouble(prop, "intake.irDistance")) {
                    intakePower = Math.pow(dataParser.parseDouble(prop, "intake.minPower"), ((1/dataParser.parseDouble(prop, "intake.accSpeed")) * (intakeTime.time(TimeUnit.MILLISECONDS) / 1000.0)));

                    if(intakePower < 0) {
                        intakePower *= -1;
                        intakePower = Range.clip(intakePower, intakePower, -dataParser.parseDouble(prop, "intake.minPower"));
                    } else {
                        intakePower = Range.clip(intakePower, dataParser.parseDouble(prop, "intake.minPower"), intakePower);
                    }
                } else {
                    intakeTime.reset();
//                    intakePower = dataParser.parseDouble(prop, "intake.power");
                }
                intake_left.setPower(intakePower);
                intake_right.setPower(-intakePower);
            }

        } else {
            intake_left.setPower(0);
            intake_right.setPower(0);
        }

    }

    public void setPowers(double power) {
        intake_left.setPower(power);
        intake_right.setPower(power);
    }

    public void init() {
        intake_right.setDirection(DcMotor.Direction.REVERSE);
        resetDeployTime();
    }

    public double getIRDistance(DistanceUnit unit) {
        return irSensor.getDistance(unit);
    }

    public void stop() {
        setPowers(0);
        deploy_left.setPower(0);
        deploy_right.setPower(0);
    }

    public double getDeployLeftPos() {
        return deploy_left.getPower();
    }
    public double getDeployRightPos() {
        return deploy_right.getPower();
    }

}