package org.firstinspires.ftc.team8375.Subsystems;


import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.team8375.dataParser;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldCanBeLocal")
public class Arm extends Subsystem {
    private Servo adjust, claw;
    private DcMotor lift_left, lift_right;
    private CRServo extend;
    private ElapsedTime flipTime = new ElapsedTime();
    private ElapsedTime resetTime = new ElapsedTime();
    private double liftLeftPos, adjustPos, flipPos, liftRightPos, extendPower, liftLeftPower, liftRightPower, liftHighLimit;
    private boolean reset, resetIsDone, clawButton, flipButton;
    private int clawOn, resetStep, flipOn;
    public Arm() {}

    @Override
    public void create() {
        adjust = hwMap.get(Servo.class, "adjust");
        claw = hwMap.get(Servo.class, "claw");
        extend = hwMap.get(CRServo.class, "extend");
        lift_left = hwMap.dcMotor.get("lift_left");
        lift_right = hwMap.dcMotor.get("lift_right");

        lift_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift_left.setDirection(DcMotorSimple.Direction.REVERSE);
        lift_right.setDirection(DcMotorSimple.Direction.FORWARD);
        liftHighLimit = dataParser.parseDouble(prop, "arm.liftHigh") - dataParser.parseDouble(prop, "arm.limitRange");
        clawOn = 1;
        flipOn = 1;
    }
    @Override
    public void stop() {
        lift_left.setPower(0);
        lift_right.setPower(0);
        extend.setPower(0);
    }

    public void run(double liftPower, double extendPower, double flipPower, boolean flipButton, boolean clawButton, boolean reset) {
        liftLeftPos = lift_left.getCurrentPosition();
        liftRightPos = lift_right.getCurrentPosition();

        //reset code here
        if(reset) {
            this.reset = true;
            resetIsDone = false;
            resetStep = 0;
        }
        if(this.reset && !reset) {
            if(!resetIsDone) {
                clawOn = 1;
                flipOn = 1;

                this.extendPower = -1;

                if(resetTime.time(TimeUnit.SECONDS) >= 1.5) {
                    resetStep = 1;
                }

                if(!lift_left.isBusy() && resetStep == 1) {
                    if(liftLeftPos == 0) {
                        lift_left.setPower(0);
                        lift_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    } else {
                        lift_left.setTargetPosition(0);
                        lift_left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        lift_left.setPower(dataParser.parseDouble(prop, "arm.liftResetPower"));
                    }
                }

                if(!lift_right.isBusy()) {
                    if (liftRightPos == 0) {
                        lift_right.setPower(0);
                        lift_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    } else {
                        lift_right.setTargetPosition(0);
                        lift_right.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        lift_right.setPower(dataParser.parseDouble(prop, "arm.liftResetPower"));
                    }
                }

                if(liftRightPos == 0 && liftLeftPos == 0 && resetStep == 1) {
                    resetIsDone = true;
                }
            }

            if(resetIsDone && this.reset) {
                lift_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift_right.setPower(0);
                lift_left.setPower(0);
                this.reset = false;
            }
        }

        if(liftPower != 0 || extendPower != 0 || clawButton || flipPower != 0) {
            if(!resetIsDone) {
                lift_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift_left.setPower(0);
                lift_right.setPower(0);
                resetIsDone = true;
                this.reset = false;
            }
        }

        //most code goes here
        if(resetIsDone) {
            resetTime.reset();

            //adjust accel/movement code
            if(flipButton && !this.flipButton) {
                flipOn *= -1;
                flipPos = 0;
                this.flipButton = true;
            }
            if(!flipButton && this.flipButton) {
                this.flipButton = false;
            }

            if (flipPower > 0) {
                if(flipTime.time(TimeUnit.MILLISECONDS) > (50 / flipPower)) {
                    flipPos += 0.01;
                    flipTime.reset();
                }
            } else if (flipPower < 0) {
                if(flipTime.time(TimeUnit.MILLISECONDS) > (100 / Math.abs(flipPower))) {
                    flipPos -= 0.01;
                    flipTime.reset();
                }
            }


            if (liftPower > 0 && liftLeftPos >= liftHighLimit) {

                this.liftLeftPower = (dataParser.parseDouble(prop, "arm.liftHigh") - liftLeftPos) / dataParser.parseDouble(prop, "arm.limitRange") * liftPower;

            } else if (liftPower < 0 && liftLeftPos < dataParser.parseDouble(prop, "arm.limitRange")) {

                this.liftLeftPower = (liftLeftPos / dataParser.parseDouble(prop, "arm.limitRange")) * liftPower;

            } else {
                this.liftLeftPower = liftPower;
            }
            if(liftPower > 0 && liftRightPos >= liftHighLimit) {

                this.liftRightPower = ((dataParser.parseDouble(prop, "arm.liftHigh") - liftRightPos) / dataParser.parseDouble(prop, "arm.limitRange")) * liftPower;

            } else if(liftPower < 0 && liftRightPos < dataParser.parseDouble(prop, "arm.limitRange")) {

                this.liftRightPower = (liftRightPos / dataParser.parseDouble(prop, "arm.limitRange")) * liftPower;

            } else {
                this.liftRightPower = liftPower;
            }

            this.extendPower = extendPower;

            if(clawButton && !this.clawButton) {
                clawOn *= -1;
                this.clawButton = true;
            }
            if(!clawButton && this.clawButton) {
                this.clawButton = false;
            }
        }

        if(clawOn > 0) {
            setServoAngle(claw, dataParser.parseDouble(prop, "arm.clawOut"));
        } else if(clawOn < 0) {
            setServoAngle(claw, dataParser.parseDouble(prop, "arm.clawIn"));
        }
        if(flipOn < 0) {
            adjustPos = dataParser.parseDouble(prop, "arm.adjustOut") + flipPos;
        } else if(flipOn > 0) {
            adjustPos = dataParser.parseDouble(prop, "arm.adjustPos") + flipPos;
        }

        adjust.setPosition(adjustPos);

        lift_left.setPower(this.liftLeftPower);
        lift_right.setPower(this.liftRightPower);
        extend.setPower(this.extendPower);
    }

    public void setServoAngle(Servo servo, double angle) {
        if(angle != 0) {
            servo.setPosition(angle / 180);
        }
    }

    public double getLiftLeftPos() {
        return lift_left.getCurrentPosition();
    }
    public double getLiftRightPos() {
        return lift_right.getCurrentPosition();
    }

}