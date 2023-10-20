package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


@TeleOp
public class MainTeleOp extends LinearOpMode {


    private IMU imu;
    //robot centric orientation
    private GamepadEx gp1, gp2;
    private double distanceFromObject;

    //do we ever initialize bot? Like with the opMode?
    Bot bot;
    private boolean isAutomatic;

    private double driveSpeed=1;

    @Override
    public void runOpMode() throws InterruptedException {

        //shouldn't imu be in Bot?
        imu = hardwareMap.get(IMU.class, "IMU?");
        gp2 = new GamepadEx(gamepad2);
        gp1 = new GamepadEx(gamepad1);

        distanceFromObject = bot.distanceSensor.getDistance(DistanceUnit.CM);
        telemetry.addData("teleOp is ", "initialized");

        waitForStart();

        while (opModeIsActive() && !isStopRequested()) {
            telemetry.addData("teleOp is ", "running");
            telemetry.update();
            gp1.readButtons();
            gp2.readButtons();
            telemetry.update();

            if(gp2.wasJustPressed(GamepadKeys.Button.START)) {
                isAutomatic = !isAutomatic;
                telemetry.addData("selected automatic driving mode",isAutomatic);
            }

            //Finite state machine enabled
            if(isAutomatic) {
                //noodle intake
                if(gp2.wasJustPressed(GamepadKeys.Button.Y)) {
                    Bot.noodles.Intake();
                    while(!Bot.box.getIsFull()) {
                        Bot.box.checkBeam();
                        Bot.noodles.Intake();
                    }
                    if(Bot.box.getIsFull()) {
                        Bot.noodles.stop();
                        bot.prepForOuttake();
                    }
                }

                //slide movement (automatic stages)
                if (gp2.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                    bot.outtake(3,bot.distanceSensor,false);
                } else if (gp2.wasJustPressed(GamepadKeys.Button.DPAD_LEFT)) {
                    bot.outtake(2,bot.distanceSensor,false);
                } else if (gp2.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT)) {
                    bot.outtake(4,bot.distanceSensor,false);
                } else if (gp2.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
                    bot.outtake(1,bot.distanceSensor,false);
                }

                //keeping second pixel deposit manual for reasons
                if(gp2.wasJustPressed(GamepadKeys.Button.A) && Bot.box.getNumPixelsDeposited()==1) {
                    Bot.box.depositSecondPixel();
                    Bot.resetOuttake();
                }

                //drone + sus code
                if(gp2.wasJustPressed(GamepadKeys.Button.B)) {
                //  Bot.suspension.hang();
                    Bot.drone.shoot();
                }
            }

            //Disabled finite state machine
            if(!isAutomatic){
                //drone movement
                if(gp2.wasJustPressed(GamepadKeys.Button.X)){
                    Bot.drone.shoot();
                    Bot.drone.reset();
                }

                //suspension
                if(gp2.wasJustPressed(GamepadKeys.Button.B)){
                  //Bot.suspension.hang();
                }

                //Box movement
                if(gp2.wasJustPressed(GamepadKeys.Button.Y)){
                    Bot.box.resetBox();
                }
                if(gp2.wasJustPressed(GamepadKeys.Button.A)){
                    bot.outtakeBox();
                }
                if(gp2.wasJustPressed(GamepadKeys.Button.A)){
                    bot.outtakeBox();
                }

                //intake
                if(gp2.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER)>0.1){
                    double power = gp2.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER);
                    while(gp2.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER)>0.1){
                        bot.intake(power);
                        power = gp2.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER);
                    }
                    bot.intake(0);
                }


                //slides
                if(gp2.getLeftY()!=0){
                    double raw = gp2.getLeftY();
                    bot.outtakeSlides(raw*1800);
                    //the max value for the joystick will be equal to 1, so the max value for runTo in slides will be 1800
                }

                //fourbar
                if(gp2.getRightY()>0){
                    bot.outtakeFourbar(gp2.getRightY());
                }
                if(gp2.getRightY()<0){
                    bot.outtakeFourbar(gp2.getLeftY());
                }
            }
            //constantly checking for drive inputs
            drive();
        }
    }

    private void drive() {
        if (gp1.wasJustReleased(GamepadKeys.Button.LEFT_STICK_BUTTON)) {
            bot.resetIMU();
        }
        driveSpeed *= 1 - 0.5 * gp1.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER);
        driveSpeed = Math.max(0, driveSpeed);

        Vector2d driveVector = new Vector2d(gp1.getLeftX(), -gp1.getLeftY()),
                turnVector = new Vector2d(
                        gp1.getRightX(), 0);
        bot.driveRobotCentric(driveVector.getX() * driveSpeed,
                driveVector.getY() * driveSpeed,
                turnVector.getX() * driveSpeed / 1.7
        );
    }
}
