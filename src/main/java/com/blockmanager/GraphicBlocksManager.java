package main.java.com.blockmanager;

import main.java.com.clustering.MyCallBack;
import main.java.com.tools.Config;
import main.java.com.tools.Helper;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class GraphicBlocksManager {
	public static void updateValues(long[] dimensions, MyCallBack callback) {
//		callback.log(dimensions.length+"D input");
		long[] numberBlocks;
		if (dimensions.length == 2) { 
			numberBlocks = new long[2];
			for (int i = 0; i < 2; i++)
				numberBlocks[i] = (Config.getOverlap()*2+dimensions[i]) / Config.getBlockSize(i)
				+ (((Config.getOverlap()*2+dimensions[i]) % Config.getBlockSize(i)) > 0 ? 1 : 0);
	
			double perspectiveRation = computeRationView(numberBlocks, Config.getBlocksSize(),callback);
			callback.log(dimensions[0]+"-"+dimensions[1]+"~ Blocks:"+numberBlocks[0]+"-"+numberBlocks[1]+" Perspective:"+perspectiveRation);
		
			Config.blocksView = BlocksManager.getBlocks(dimensions, numberBlocks, Config.getBlocksSize(),
			Config.getOverlap(), perspectiveRation);
			Config.totalBlocks = numberBlocks[0]*numberBlocks[1];
			} else {
			numberBlocks = computeGraphicBlocks(dimensions, Config.getBlocksSize());
			Config.blocksView = BlocksManager.getBlocks(numberBlocks, computeSizePreviewBox(numberBlocks));
			Config.totalBlocks = numberBlocks[0]*numberBlocks[1]+numberBlocks[2];
		}
		
		Config.previewPreferedHeight = (int) (numberBlocks[1]*Config.getBlockSize(1));
	}
	
	public static double computeRationView(long[] numberBlocks, long[] blocksSize,MyCallBack callback) {
		callback.log("Panel Width:"+Config.PREVIEW_PANEL_WIDTH+" NumberBlocks:"+numberBlocks[0]+" BlocksSize:"+blocksSize[0]);
		return Config.PREVIEW_PANEL_WIDTH / (1.0* numberBlocks[0] * blocksSize[0]);
	}

	public static long[] computeBlocksPerDimension(long[] dimensions, long[] blocksDimensions) {
		long[] numberBlocksPerDimension = new long[dimensions.length];
		for (int i = 0; i < dimensions.length; i++) {
			numberBlocksPerDimension[i] = dimensions[i] / blocksDimensions[i]
					+ ((dimensions[i] % blocksDimensions[i]) > 0 ? 1 : 0);
		}
		return numberBlocksPerDimension;
	}

	public static long countTotalBlocks(long[] numberBlocksPerDimension) {
		long totalBlocks = 1;
		for (long elm : numberBlocksPerDimension)
			if(elm>0)totalBlocks *= elm;
			else totalBlocks+=elm;
		return totalBlocks;
	}

	// return 3D long [1]-columns [2]-Rows [3]-Columns in last row
	public static long[] computeGraphicBlocks(long[] dimensions, long[] blocksDimensions) {
		long[] numberBlocksPerDimension = computeBlocksPerDimension(dimensions, blocksDimensions);
		long totalBlocks = countTotalBlocks(numberBlocksPerDimension);
		long sqrt = (long) Math.sqrt(totalBlocks);
		if (sqrt * Config.MINIMUM_BOX_SIZE < Config.PREVIEW_PANEL_WIDTH) {
			return new long[] { sqrt, sqrt, totalBlocks % sqrt };
		} else {
			long columns = Config.PREIVIEW_PANEL_HEIGHT / Config.MINIMUM_BOX_SIZE;
			long rows = totalBlocks / columns;
			long rest = totalBlocks % columns;
			return new long[] { columns, rows, rest };
		}
	}

	public static int computeSizePreviewBox(long[] numberPreviewBlocks) {
		return (int) (Config.PREIVIEW_PANEL_HEIGHT / numberPreviewBlocks[0]);
	}

	public static long[] get2DDimensions(Img<FloatType> file,MyCallBack callback) {
		long[] dims = Helper.getDimensions(file);
		String logString = Helper.logArray(dims);
		callback.log("Dims: " + logString);
		for (int i = 2; i < dims.length; i++) {
			dims[i % 2] = dims[i % 2] * dims[i];
		}
		long[] result = new long[] { dims[0], dims[1] };
		logString = Helper.logArray(result);
		callback.log("2D - Dims: " + logString);
		return result;
	}
	
	public long[] divideIntoBlocks( final long[] imgSize, final long[] kernelSize, MyCallBack callback )
	{
		long[] blockSize = Config.getBlocksSize();
		final int numDimensions = imgSize.length;
		
		// compute the effective size & local offset of each block
		// this is the same for all blocks
		final long[] effectiveSizeGeneral = new long[ numDimensions ];
		final long[] effectiveLocalOffset = new long[ numDimensions ];
		
		for ( int d = 0; d < numDimensions; ++d )
		{
			effectiveSizeGeneral[ d ] = blockSize[ d ] - kernelSize[ d ] + 1;
			
			if ( effectiveSizeGeneral[ d ] <= 0 )
			{
				callback.log( "Blocksize in dimension " + d + " (" + blockSize[ d ] + ") is smaller than the kernel (" + kernelSize[ d ] + ") which results in an negative effective size: " + effectiveSizeGeneral[ d ] + ". Quitting." );
				return null;
			}
			
			effectiveLocalOffset[ d ] = kernelSize[ d ] / 2;
		}
		
		// compute the amount of blocks needed
		final long[] numBlocks = new long[ numDimensions ];

		for ( int d = 0; d < numDimensions; ++d )
		{
			numBlocks[ d ] = imgSize[ d ] / effectiveSizeGeneral[ d ];
			
			// if the modulo is not 0 we need one more that is only partially useful
			if ( imgSize[ d ] % effectiveSizeGeneral[ d ] != 0 )
				++numBlocks[ d ];
		}
		

		return numBlocks;
	}
}