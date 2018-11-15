package main.java.com.imageaccess;

import java.io.File;
import java.util.List;

import main.java.com.controllers.items.JFile;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;

public class Loader {
	final File file;
	final SpimData2 spimData;
	final JFile jfile;

	public SpimData2 getSpimData() {
		return spimData;
	}
	public File getFile() {
		return file;
	}
	
	private Loader(File file, SpimData2 spimData, JFile jfile) {
		this.jfile = jfile;
		this.file = file;
		this.spimData = spimData;
	}
	
	public RandomAccessibleInterval< FloatType > fuse(final SpimData2 spimData, final List< ViewId > viewIds, final Interval interval )
	{
		switch (jfile.getExtension()) {
		case TIF:
			//TODO
			break;
		case XML:
			return LoadXML.fuse(spimData, viewIds, interval);

		default:
			break;
		}
		return null;
	}

	

	public static class Builder {
		public static Loader load(JFile jfile) {
			switch (jfile.getExtension()) {
			case TIF:
				//TODO
				break;
			case XML:
				final LoadXML load = new LoadXML(jfile.getAll() );
				return new Loader(load.file, load.spimData,jfile);

			default:
				break;
			}
			return null;
		}
	}

}