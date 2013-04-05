package Interfaces;

/**
 * This interface is used by external algorithms to protect the
 * various public methods of TabbedCanvas.
 * Using this interface, external algorithms, importers, exporters 
 * and filters  may only access the heightmap.
 * 
 * @author James Robinson 
 * @version 1.0
 */
public interface ICanvasAlg
{
	int[][] getHeightMap();
	void setHeightMap(int[][] heightmap);
	void refreshMiniView(int[][] heightmap);
	void resetProgressBar();
	void setProgressBar(int max);
	void increaseProgressBar();
	void setPreviewMap(int[][] heightmap, int width);
	int[][] getPreviewMap();
	
	// tools
	int[][] normaliseMap(int[][] map, int strength);
	void amendLog(String s);
	void displayError(String e, String p, String s);
	int[][] cloneArray(int[][] source);
}
