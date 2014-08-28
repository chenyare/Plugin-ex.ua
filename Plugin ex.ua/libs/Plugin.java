package player.plugin;

import java.awt.image.BufferedImage;

/**
 *  Perfect Player plugin interface.
 *  @author Sergey Nikitin
 *  @version 0.1.0
 */
public interface Plugin {
	
	/**
	 * @return plugin display name
	 */
	public String getPluginName();

	/**
	 * Download and parse current site's URL.
	 * Prepare internal data structures (names, URLs, types, thumbs).
	 * @return true if page was downloaded successfully, otherwise false 
	 */
	public boolean refresh();
	
	/**
	 * @return current page links names
	 */
	public String[] getNames();

	/**
	 * @return current page links URLs
	 */
	public String[] getURLs();
	
	/**
	 * Return current page links types (video links or folders).
	 * @return true for folders, false for videos
	 */
	public boolean[] getTypes();
	
	/**
	 * @return current page links thumbs
	 */
	public BufferedImage[] getThumbs();
	
	/**
	 * Enter to itemNum folder in current page.
	 * Method should call {@link refresh()} in the end.
	 */
	public void selectItem(int itemNum);
	
	/**
	 * Go to previous page.
	 * Method should call {@link refresh()} in the end.
	 * @return true if successfully went, otherwise false
	 */
	public boolean previousPage();
	
	/**
	 * Go to next page.
	 * Method should call {@link refresh()} in the end.
	 * @return true if successfully went, otherwise false
	 */
	public boolean nextPage();
	
}
