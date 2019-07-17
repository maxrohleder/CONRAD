package MR;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.opencl.OpenCLProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.phantom.renderer.ParallelProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.ImageJ;


class helpers{
	/**
	 * helper visualizing a Grid3D
	 * @param img
	 */
	public static void showGrid(Grid3D img, String name) {
		new ImageJ();
		/*
		ImagePlus image = ImageUtil.wrapGrid3D(img, name);
		Calibration cal = image.getCalibration();
		cal.xOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsX();
		cal.yOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsY();
		cal.zOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsZ();
		cal.pixelWidth = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingX();
		cal.pixelHeight = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingY();
		cal.pixelDepth = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingZ();
		*/
		img.show(name);
	}
	
	public static void main(String []args) {
		
		Grid3D easy = new Grid3D(3, 3, 3);
		easy.setAtIndex(0, 0, 0, 255);
		new ImageJ();
		easy.show();
		
		
		Grid3D img = new Grid3D(300, 300, 300);
		int[] origin = new int[] {150, 150, 150};
		
		for (int i = 0; i < img.getSize()[0]; i++) {
			for (int j = 0; j < img.getSize()[1]; j++) {
				for (int k = 0; k < img.getSize()[2]; k++) {
					int dist = euk_dist(origin, new int[] {i, j, k});
					img.setAtIndex(i, j, k, dist*255);
				}
			}
		}

		ImagePlus imp = ImageUtil.wrapGrid3D(img, "TEST");
		imp.show();
		
	}
	public static int euk_dist(int[] a, int[] b) {
		int dst = (int) Math.sqrt((a[0]-b[0]) * (a[0]-b[0]) + 
				(a[1] - b[1]) * (a[1] - b[1]) + 
				 (a[2] - b[2]) + (a[2] - b[2]));
		return (dst>20) ? 0 : 1;
	}
}