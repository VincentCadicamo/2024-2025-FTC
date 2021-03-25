package org.firstinspires.ftc.teamcode.bots;

import android.graphics.Point;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.autonomous.AutoDot;
import org.firstinspires.ftc.teamcode.autonomous.AutoRoute;
import org.firstinspires.ftc.teamcode.calibration.MotorReductionBot;
import org.firstinspires.ftc.teamcode.odometry.RobotCoordinatePosition;
import org.firstinspires.ftc.teamcode.skills.RingDetector;

import static java.lang.Math.abs;

// Control Hub ADB Terminal Command for Reference
// adb.exe connect 192.168.43.1:5555

public class UltimateBot extends YellowBot {
    private Servo wobbleSwing = null;
    private Servo wobbleClaw1 = null;
    private Servo wobbleClaw2 = null;
    private Servo ringCamera = null;
    private Servo shooterServo = null;
    private Servo ringGuard = null;
    private DcMotorEx shooter = null;

    private SwingPosition swingPosition = SwingPosition.Init;
    private static double SWING_BACK_POS = 1;
    private static double SWING_PLACE_POS = 0;
    private static double SWING_LIFT_AND_HOLD = 0.25;
    private static double SWING_LIFT_WALL = 0.45;
    private static double SHOOT_SERVO = 0.7;

    private static int TIMEOUT = 2500;
    private static int TIMEOUT_LONGER = 3000;
    private static int TIMEOUT_SHORTER = 1500;


    private static double CAMERA_RIGHT_LINE = 0.35;
    private static double CAMERA_LEFT_LINE = 0.5;

    private RingDetector rf = null;


    /* Constructor */
    public UltimateBot() {

    }

    @Override
    public void init(LinearOpMode owner, HardwareMap ahwMap, Telemetry telemetry) throws Exception {
        super.init(owner, ahwMap, telemetry);

        try {
            DcMotorEx intake = getIntakeMotor();
            intake.setDirection(DcMotor.Direction.FORWARD);
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            intake.setPower(0);
        } catch (Exception ex) {
            throw new Exception("Issues with intake. Check the controller config", ex);
        }

        try {
            shooter = hwMap.get(DcMotorEx.class, "shooter");
            shooter.setDirection(DcMotor.Direction.REVERSE);
            shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            shooter.setVelocity(0);
        } catch (Exception ex) {
            throw new Exception("Issues with shooter. Check the controller config", ex);
        }
        // claw starts closed
        try {
            wobbleClaw1 = hwMap.get(Servo.class, "claw1");
            wobbleClaw1.setPosition(0);
        } catch (Exception ex) {
            throw new Exception("Issues with wobbleClaw1. Check the controller config", ex);
        }

        try {
            wobbleClaw2 = hwMap.get(Servo.class, "claw2");
            wobbleClaw2.setPosition(1);
        } catch (Exception ex) {
            throw new Exception("Issues with wobbleClaw2. Check the controller config", ex);
        }
        // camera has no init position so can manually move
        try {
            ringCamera = hwMap.get(Servo.class, "camera");
        } catch (Exception ex) {
            throw new Exception("Issues with ringCamera. Check the controller config", ex);
        }

        try {
            shooterServo = hwMap.get(Servo.class, "shoot");
            shooterServo.setPosition(SHOOT_SERVO);
        } catch (Exception ex) {
            throw new Exception("Issues with shooterServo. Check the controller config", ex);
        }

        try {
            wobbleSwing = hwMap.get(Servo.class, "wobble");
            wobbleSwing.setPosition(SWING_BACK_POS);
        } catch (Exception ex) {
            throw new Exception("Issues with wobbleSwing. Check the controller config", ex);
        }

        try {
            ringGuard = hwMap.get(Servo.class, "rguard");
            ringGuard.setPosition(1);
        } catch (Exception ex) {
            throw new Exception("Issues with ringGuard. Check the controller config", ex);
        }

        telemetry.addData("Init", "Ultimate is ready");
    }

    public DcMotorEx getIntakeMotor(){
        return rightOdo;
    }

    public double getShooterVelocity(){
        return shooter.getVelocity();
    }


    @BotAction(displayName = "Move Intake", defaultReturn = "")
    public void intake() {
        DcMotorEx intake = getIntakeMotor();
        if (intake != null) {
            intake.setPower(0.9);
        }
    }

    @BotAction(displayName = "Move Intake Reverse", defaultReturn = "")
    public void intakeReverse() {
        DcMotorEx intake = getIntakeMotor();
        if (intake != null) {
            intake.setPower(-0.7);
        }
    }

    @BotAction(displayName = "Stop Intake", defaultReturn = "")
    public void stopintake() {
        DcMotorEx intake = getIntakeMotor();
        if (intake != null) {
            intake.setPower(0);
        }
    }

    @BotAction(displayName = "Move Shooter", defaultReturn = "")
    public void shooter() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.8);
        }
    }

    @BotAction(displayName = "Move Shooter High", defaultReturn = "")
    public void shooterhigh() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.8);
        }
    }

    @BotAction(displayName = "Move Peg Shooter Low", defaultReturn = "")
    public void shooterpeglow() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.65);
        }
    }

    @BotAction(displayName = "Move Shooter Med", defaultReturn = "")
    public void shootermed() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.7);
        }
    }

    @BotAction(displayName = "Move Shooter Low", defaultReturn = "")
    public void shooterlow() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.75);
        }
    }

    @BotAction(displayName = "Move Shooter Lower", defaultReturn = "")
    public void shooterlower() {
        if (shooter != null) {
            shooter.setVelocity(MAX_VELOCITY*0.73);
        }
    }


    @BotAction(displayName = "Shoot", defaultReturn = "")
    public void shootServo() {
        ElapsedTime runtime = new ElapsedTime();
        if (shooterServo != null) {
            shooterServo.setPosition(SHOOT_SERVO - 0.4);
            runtime.reset();
            while (runtime.milliseconds() <= 250) {

            }
            shooterServo.setPosition(SHOOT_SERVO);
        }
    }

    @BotAction(displayName = "Guard Down", defaultReturn =  "")
    public void guardDown() {
        ringGuard.setPosition(0.03);
    }

    @BotAction(displayName = "Guard Up", defaultReturn =  "")
    public void guardUp() {
        ringGuard.setPosition(1);
    }

    @BotAction(displayName = "Stop Shooter", defaultReturn = "")
    public void stopshooter() {
        if (shooter != null) {
            shooter.setPower(0);
        }
    }

    @BotAction(displayName = "Close Claw", defaultReturn = "")
    public void closeWobbleClaw() {
        if ((wobbleClaw1 != null) && (wobbleClaw2 != null)) {
            wobbleClaw1.setPosition(0);
            wobbleClaw2.setPosition(1);
        }
    }

    @BotAction(displayName = "Open Claw", defaultReturn = "")
    public void openWobbleClaw() {
        if ((wobbleClaw1 != null) && (wobbleClaw2 != null)) {
            wobbleClaw1.setPosition(1);
            wobbleClaw2.setPosition(0);
        }
    }

    @BotAction(displayName = "Camera Left", defaultReturn = "")
    public void leftRingCamera() {
        if (ringCamera != null) {
            ringCamera.setPosition(CAMERA_LEFT_LINE);
        }
    }

    @BotAction(displayName = "Camera Right", defaultReturn = "")
    public void rightRingCamera() {
        if (ringCamera != null) {
            ringCamera.setPosition(CAMERA_RIGHT_LINE);
        }
    }

    // WOBLLE FUNCTIONS
    @BotAction(displayName = "Wobble Little Up", defaultReturn = "")
    public void wobbleLittleUp() {
        if (wobbleSwing != null) {
            double currposition = wobbleSwing.getPosition();
            currposition = currposition + 0.05;
            wobbleSwing.setPosition(currposition);
        }
    }

    @BotAction(displayName = "Wobble Little Down", defaultReturn = "")
    public void wobbleLittleDown() {
        if (wobbleSwing != null) {
            double currposition = wobbleSwing.getPosition();
            currposition = currposition - 0.05;
            wobbleSwing.setPosition(currposition);
        }
    }

    @BotAction(displayName = "Init WobbleSwing", defaultReturn = "")
    public void backWobbleSwing() {
        if (wobbleSwing != null) {
            wobbleSwing.setPosition(SWING_BACK_POS);
        }
    }

    @BotAction(displayName = "Place Wobble", defaultReturn = "")
    public void forwardWobbleSwing() {
        if (wobbleSwing != null) {
            wobbleSwing.setPosition(SWING_PLACE_POS);
        }
    }

    @BotAction(displayName = "Lift Wobble Up Hold", defaultReturn = "")
    public void liftAndHoldWobbleSwing() {
        if (wobbleSwing != null) {
            wobbleSwing.setPosition(SWING_LIFT_AND_HOLD);
        }
    }

    @BotAction(displayName = "Lift Wobble Wall", defaultReturn = "")
    public void liftWobbleWall() {
        if (wobbleSwing != null) {
            wobbleSwing.setPosition(SWING_LIFT_WALL);
        }
    }

    // compound claw up for wall
    @BotAction(displayName = "Lift Wall and Grab", defaultReturn = "")
    public void liftWallGrab() {
        ElapsedTime runtime = new ElapsedTime();
        closeWobbleClaw();
        while (runtime.milliseconds() <= 300) {
        }
        liftWobbleWall();
    }

    public double getWobblePos() {
        if (wobbleSwing != null) {
            return wobbleSwing.getPosition();
        } else {
            return (-1);
        }
    }


//    @BotAction(displayName = "Green Light", defaultReturn = "")
//    public void signalOK() {
//        getLights().OK();
//        ElapsedTime timer = new ElapsedTime();
//        timer.reset();
//        while (timer.seconds() < 1) {
//
//        }
//        getLights().none();
//    }


    public void initDetector(String side, LinearOpMode caller) {
        try {
            rf = new RingDetector(this.hwMap, side, caller, this.namedCoordinates, telemetry);
        }
        catch (Exception ex){
            telemetry.addData("Error", String.format("Unable to initialize Detector. %s", ex.getMessage()));
            telemetry.update();
        }
    }

    public void stopDetection() {
        if (rf != null) {
            rf.stopDetection();
        }
    }

    public void initDetectorThread(String side, LinearOpMode caller) {
        try {
            rf = new RingDetector(this.hwMap, side, caller, this.namedCoordinates, telemetry);
            Thread detectThread = new Thread(rf);
            detectThread.start();
        } catch (Exception ex) {
            telemetry.addData("Error", String.format("Unable to initialize Detector thread. %s", ex.getMessage()));
            telemetry.update();
        }
    }


    ///get results of detection on the thread
    @BotAction(displayName = "Get Detection Result", defaultReturn = "B")
    public AutoDot getDetectionResult() {
        AutoDot target = null;
        if (rf != null) {
            rf.stopDetection();
            target = rf.getRecogZone();
        }

        telemetry.addData("Detected Zone: ", target.getDotName());
        telemetry.addData("Detected X: ", target.getX());
        telemetry.addData("Detected Y: ", target.getY());
        telemetry.update();
        return target;
    }


//    @BotAction(displayName = "Detection Lights", defaultReturn = "")
//    public void displayDetectionLights() {
//        rf.displayLights();
//    }

//    @BotAction(displayName = "Lights Off", defaultReturn = "")
//    public void lightsOff() {
//        getLights().none();
//    }

    ///use for non-threaded detection
    @BotAction(displayName = "Detect Stack", defaultReturn = "B")
    public AutoDot detectStack(String side) {
        AutoDot target = null;
        if (rf != null) {
            try {
                target = rf.detectRing(2, side, telemetry, owner);
            } finally {
//                if (rf != null) {
//                    rf.stopDetection();
//                }
            }
        }
        return target;
    }


    public int checkRingPos() {
        float ringCenter = 0;
        int strafeAmnt = 0;
        if (rf != null) {
            try {
                ringCenter = rf.detectRingsPos(2, telemetry, owner);
            } finally {
//                if (rf != null) {
//                    rf.stopDetection();
//                }
            }
        }

        if (ringCenter > 15) {
            strafeAmnt = 1;
        } else {
            strafeAmnt = -1;
        }

        return strafeAmnt;
    }

    @BotAction(displayName = "Adjust to Ring Stack", defaultReturn = "")
    public void adjustToCameraRing(RobotCoordinatePosition locator){
        int strafeAmnt = checkRingPos();

        //wait for the locator to stabilize
        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        while(timer.milliseconds() < 500 && this.owner.opModeIsActive()){
        }

        //get the orientation as the locator knows it. we'll use it later for corrections
        double originalOrientation = locator.getAdjustedCurrentHeading();
        double originalx = locator.getX();
        Log.d("UltimateBot", String.format("original orientation: %.2f", originalOrientation));

        //strafe to align the robot with ring stack
        strafeTo(0.2, originalx + strafeAmnt, true);

    }

    public void shootPegContinuous(RobotCoordinatePosition locator){
        // start shooter
        shooterpeglow();

        //wait for the locator to stabilize
        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        while(timer.milliseconds() < 500 && this.owner.opModeIsActive()){
        }

        //get the orientation as the locator knows it. we'll use it later for corrections
        double originalOrientation = locator.getAdjustedCurrentHeading();

        double strafeSpeed = 0.2;
        double spinSpeed = 0.1;
        double strafeToFirst = 7;
        double strafeToSecond = 13;
        double strafeToLast = 22;

        double currentPos = this.getHorizontalOdometer();
        double distancefinal = strafeToLast * COUNTS_PER_INCH_REV;
        double distancefirst = strafeToFirst * COUNTS_PER_INCH_REV;
        double distancesecond = strafeToSecond * COUNTS_PER_INCH_REV;

        MotorReductionBot calib = null;
        calib = getCalibConfig().getStrafeLeftReduction();

        double overage = 0;
        double target = currentPos + distancefinal;
        double target1 = currentPos + distancefirst;
        double target2 = currentPos + distancesecond;
        boolean stop = false;

        // breaking and slowing down variables
        double slowdownMark = target * 0.85;
        double ticksAdjustment = MAX_VELOCITY_PER_PROC_DELAY*strafeSpeed;
        slowdownMark = slowdownMark - ticksAdjustment;
        int step = 0;
        double power = 0;
        double minSpeed = 0.05;
        double speedIncrement = 0.05;
        double speedDropStep = 0.05;
        double currentSpeed = 0;

        boolean shotfirstalready = false;
        boolean shotsecondalready = false;

        //move
        while (!stop && this.owner.opModeIsActive()) {
            currentPos = this.getHorizontalOdometer();
            if (currentPos >= target) {
                stop = true;
            }
            if (currentPos >= slowdownMark) {
                // slow down
                step++;
                power = currentSpeed - speedDropStep*step;
                if (power <= minSpeed) {
                    power = minSpeed;
                }
            } else {
                // accelerate
                if (power+speedIncrement <= strafeSpeed) {
                    power+=speedIncrement;
                    currentSpeed = power;
                }
            }

            // shoot at correct points
            if (abs(currentPos-target1)/COUNTS_PER_INCH_REV <= 0.5 && !shotfirstalready) {
                shootServo();
                shotfirstalready = true;
            }
            // shoot at correct points
            if (abs(currentPos-target2)/COUNTS_PER_INCH_REV <= 0.5 && !shotsecondalready) {
                shootServo();
                shotsecondalready = true;
            }

            // move the motors
            this.backLeft.setVelocity(MAX_VELOCITY * -power * calib.getLB());
            this.backRight.setVelocity(MAX_VELOCITY * power * calib.getRB());
            this.frontLeft.setVelocity(MAX_VELOCITY * power * calib.getLF());
            this.frontRight.setVelocity(MAX_VELOCITY * -power * calib.getRF());

        }

        shootServo();
        stop();
        double newPos = this.getHorizontalOdometer();
        double diff = abs(newPos - target);
        overage = diff / distancefinal * 100;

    }

    public void shootPegSequence(RobotCoordinatePosition locator){

        //start shooter
        shooterpeglow();

        //wait for the locator to stabilize
        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        while(timer.milliseconds() < 300 && this.owner.opModeIsActive()){
        }

        //get the orientation as the locator knows it. we'll use it later for corrections
        double originalOrientation = locator.getAdjustedCurrentHeading();
        Log.d("UltimateBot", String.format("original orientation: %.2f", originalOrientation));
        double strafeSpeed = 0.3;
        double spinSpeed = 0.1;
        double strafeToFirst = 12.5;
        double strafeBetweenPegs = 5;
        double strafeToLastPeg = 6;
        //FirstPeg
        //strafe to align the robot with the first peg
        strafeTo(strafeSpeed, strafeToFirst, true);
        //let locator settle
        timer.reset();
        while(timer.milliseconds() < 400 && this.owner.opModeIsActive()){
        }
        double newOrientation = locator.getAdjustedCurrentHeading();
        int marginError = 4;
        Log.d("UltimateBot", String.format("newOrientation 1: %.2f", newOrientation));
        //spin to the desired orientation
        double diff = newOrientation - originalOrientation;
        if (abs(diff) > marginError) {
            double updated = originalOrientation + diff/2;
            BotMoveProfile profileSpin = BotMoveProfile.getFinalHeadProfile(updated, spinSpeed, locator);
            spin(profileSpin, locator);
        }
        //shoot
        shootServo();

        //Second Peg
        //strafe to the next peg
        strafeTo(strafeSpeed, strafeBetweenPegs, true);
//        //let locator settle
        timer.reset();
        while(timer.milliseconds() < 400 && this.owner.opModeIsActive()){
        }
        newOrientation = locator.getAdjustedCurrentHeading();
        Log.d("UltimateBot", String.format("newOrientation 2: %.2f", newOrientation));
//        spin to the desired orientation
        diff = newOrientation - originalOrientation;
        if (abs(diff ) > marginError) {
            double updated = originalOrientation + diff/2;
            BotMoveProfile profileSpin = BotMoveProfile.getFinalHeadProfile(updated, spinSpeed, locator);
            spin(profileSpin, locator);
        }
        //shoot
        shootServo();

        //Third Peg
        //strafe to the next peg
        strafeTo(strafeSpeed, strafeToLastPeg, true);
        //let locator settle
        timer.reset();
        while(timer.milliseconds() < 400 && this.owner.opModeIsActive()){
        }
        newOrientation = locator.getAdjustedCurrentHeading();
        Log.d("UltimateBot", String.format("newOrientation 2: %.2f", newOrientation));
        //spin to the desired orientation
        diff = newOrientation - originalOrientation;
        if (Math.abs(diff ) > marginError) {
            double updated = newOrientation - 2;
            BotMoveProfile profileSpin = BotMoveProfile.getFinalHeadProfile(updated, spinSpeed, locator);
            spin(profileSpin, locator);
        }
        //shoot
        shootServo();

    }

}
