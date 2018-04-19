package master;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import Helpers.Portion;
import ij.ImageJ;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import tools.Helper;

public class SplitImageIntoBlocs {

	public SplitImageIntoBlocs() throws ImgIOException {
		Helper.sigma = 6;
//		String string = "img/mri-stack.tif";
		String string = "img/DrosophilaWing.tif";
		Img<FloatType> image = new ImgOpener().openImg(string, new FloatType());
		ImageJFunctions.show(image);

		final Img<FloatType> resultImage  = new CellImgFactory< FloatType >( 64 ).create(Helper.getDimensions(image), new FloatType());
//		ImageJFunctions.show(resultImage);

		final int numberBlocs = 2;
		int[] blocks = new int[image.numDimensions()];
		Arrays.fill(blocks, numberBlocs);

		ArrayList<Portion> portions = splitImageEnBlocs(image,blocks);
		final String processFolder = "processImages";
		saveImages(portions,processFolder);
//		Helper.showImagesInFolder(processFolder);
		ArrayList<Img<FloatType>> images = Helper.getImagesFromFolder(processFolder);
		
		for (Img<FloatType> im :images) {
			ImageJFunctions.show(im);
		}

		processImages(images);
		processBlocs(portions,resultImage);
		combineBlocs(resultImage,portions,images);
		ImageJFunctions.show(resultImage).setTitle("Result Image");


	}

	
	private void processImages(ArrayList<Img<FloatType>> images) {
		for(Img<FloatType> image:images) {
			RandomAccessible<FloatType> infiniteImg = Views.extendMirrorSingle(image);
			RandomAccessibleInterval<FloatType> view = Views.interval(infiniteImg,image);
			
		try {
			Gauss3.gauss(Helper.sigma, view,
					image);
		} catch (IncompatibleTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	}


	private void combineBlocs(Img<FloatType> resultImage, ArrayList<Portion> portions,
			ArrayList<Img<FloatType>> images) {

		
        
		RandomAccessible<FloatType> infiniteResult = Views.extendMirrorSingle(resultImage);
//		ImageJFunctions.show(Views.interval(infiniteResult, resultImage));
		for (int i = 0; i < portions.size(); i++) {
//	ImageJFunctions.show(images.get(i)).setTitle("part result: "+i);
			IterableInterval<FloatType> currentFinalInput = Views.offsetInterval(images.get(i), Helper.add(portions.get(i).getPosition(),Helper.sigma),portions.get(i).getSize());
			
			
	        IterableInterval<FloatType> currentFinalView = Views.offsetInterval(infiniteResult, portions.get(i).getPosition(),portions.get(i).getSize());
System.out.println(portions.get(i));
	        Cursor< FloatType > cursorInput = currentFinalInput.cursor();
	        Cursor< FloatType > cursorOutput = currentFinalView.cursor();
	        while ( cursorInput.hasNext())
	        {
	            cursorInput.fwd();
	            cursorOutput.fwd();
	            FloatType x = cursorInput.get();
	            FloatType y = cursorOutput.get();
	            y.set(x );
	        }
	

	        ImageJFunctions.show(resultImage).setTitle("result"+i);
			
		}

		
	}

	public static void main(String[] args) throws ImgIOException {
		new ImageJ();
		new SplitImageIntoBlocs();
	}

	private void processBlocs(ArrayList<Portion> portions, Img<FloatType> resultImage) {
		for(Portion portion:portions) {
			
			try {
				Gauss3.gauss(Helper.sigma, portion.getView(),
						Views.offsetInterval(resultImage, portion.getPosition(), portion.getSize()));
			} catch (IncompatibleTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static ArrayList<Portion> splitImageEnBlocs(RandomAccessibleInterval<FloatType> input,
			 int[] blocs) {

		RandomAccessible<FloatType> infiniteImg = Views.extendMirrorSingle(input);
		ArrayList<Portion> portions = new ArrayList<Portion>();

		long[] blocSize = new long[blocs.length];
		for (int i = 0; i < blocs.length; i++) {
			blocSize[i] = (int) (input.dimension(i) / blocs[i]);
		}
		 LocalizingZeroMinIntervalIterator iterator = new LocalizingZeroMinIntervalIterator(blocs) ;
		while (iterator.hasNext()) {
			iterator.fwd();
			System.out.println(iterator.toString());
			long[] currentPosition = new long[3];
			long[] maxPosition = new long[3];
			for (int d = 0; d < blocs.length; d++) {
				System.out.print(d+" ");
				currentPosition[d] = blocSize[d] * iterator.getIntPosition(d)-Helper.sigma ;
				maxPosition[d] = blocSize[d] * (1+iterator.getIntPosition(d)) +Helper.sigma;
			}
			System.out.println();

		RandomAccessibleInterval<FloatType> view = Views.interval(infiniteImg,currentPosition, maxPosition);
	
				portions.add(new Portion(view, currentPosition,maxPosition,blocSize));
			}
		
		return portions;
	}
	
	private void saveImages(ArrayList<Portion> portions, String processFolder) {
		File theDir = new File(processFolder);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + theDir.getName());
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
		
		for (int k = 0; k < portions.size(); k++) {
			String imgName = processFolder +"/"+k + "-part.tif";
			try {
				ij.IJ.save(ImageJFunctions.wrap(portions.get(k).getView(), ""), imgName);
			}
			catch (Exception exc) {
				System.out.println("error "+k);
				exc.printStackTrace();
			}
		}
	}
	

	
}
