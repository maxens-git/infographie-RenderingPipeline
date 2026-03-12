package renderer.core.camera;

import renderer.algebra.Matrix;
import renderer.algebra.SizeMismatchException;
import renderer.algebra.Vector;


/**
 * The Transformation class represents a transformation in 3D space.
 * author: cdehais
 */
public class Transformation {

    /**
     * The world to camera matrix.
     */
    private Matrix worldToCamera;
    /**
     * The 3x4 projection matrix.
     */
    private Matrix projection;
    /**
     * The 3x3 calibration matrix.
     */
    private Matrix calibration;

    /**
     * Creates a new Transformation object.
     */
    public Transformation() {
        final int w2cDim = 4;
        worldToCamera = Matrix.createIdentity("W2C", w2cDim);
        final int projRows = 3;
        final int projCols = 4;
        projection = new Matrix("P", projRows, projCols);
        final int calibDim = 3;
        calibration = Matrix.createIdentity("K", calibDim);
    }

    /**
     * Sets the lookAt transformation.
     * @param eye a 3D vector representing the eye position
     * @param lookAtPoint a 3D vector representing the point to look at
     * @param up a 3D vector representing the up direction
     */
    public void setLookAt(final Vector eye, final Vector lookAtPoint, final Vector up) {
        try {
            // compute rotation
            // origine = eye
            Vector z = lookAtPoint.subtract(eye).normalize();

            Vector x = up.normalize().cross(z);

            Vector y = z.cross(x);

            Matrix N = new Matrix(3, 3);
            N.setCol(0, x);
            N.setCol(1, y);
            N.setCol(2, z);
            Matrix Nt = N.transpose();

            // compute translation
            Vector T = Nt.multiply(eye).scale(-1);

            for (int i=0; i < 3; i++) {
                for (int j=0; j < 3; j++) {
                    worldToCamera.set(i,j,Nt.get(i,j));
                }
            }
            for (int j=0; j < 3; j++) {
                worldToCamera.set(3,j,0);
            }
            for (int i=0; i < 3; i++) {
                    worldToCamera.set(i,3,T.get(i));
            }
            worldToCamera.set(3,3,1);


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Modelview matrix:\n" + worldToCamera);
    }

    /**
     * Sets the projection matrix.
     */
    public void setProjection() {
        // TODO
        projection.set(0, 0, 1);
        projection.set(1, 1, 1);
        projection.set(2, 2, 1); 



        System.out.println("Projection matrix:\n" + projection);
    }

    /**
     * Sets the calibration matrix.
     * @param focal the focal length
     * @param width the width of the image
     * @param height the height of the image
     */
    public void setCalibration(double focal, double width, double height) {

        // TODO
        this.calibration.set(0, 0, focal);
        this.calibration.set(1, 1, focal);
        this.calibration.set(0, 2, width / 2);
        this.calibration.set(1, 2, height / 2);

        System.out.println("Calibration matrix:\n" + calibration);
    }

    /**
     * Projects the given 3 dimensional point onto the screen.
     * The resulting Vector as its (x,y) coordinates in pixel, and its z coordinate
     * is the depth of the point in the camera coordinate system.
     * @param p a 3d vector representing a point
     * @return the projected point as a 3d vector, with (x,y) the pixel
     * coordinates and z the depth
     * @throws SizeMismatchException if the size of the input vector is not 3
     */
    public Vector projectPoint(Vector p) throws SizeMismatchException {
        Vector p4 = p.homogeneousPoint() ;
        Vector ps = new Vector(3);

        p4 = worldToCamera.multiply(p4);

        ps = projection.multiply(p4);

        ps = calibration.multiply(ps);

        ps.set(0, ps.get(0)/ps.get(2));
        ps.set(1, ps.get(1)/ps.get(2));

        return ps;
    }

    /**
     * Transform a vector from world to camera coordinates.
     * @param v the vector to transform
     * @return the transformed vector
     * @throws SizeMismatchException if the size of the input vector is not 3
     */
    public Vector transformVector(final Vector v) {
        // Doing nothing special here because there is no scaling
        final Matrix m = worldToCamera.getSubMatrix(0, 0, 3, 3);
        return m.multiply(v);
    }

}
