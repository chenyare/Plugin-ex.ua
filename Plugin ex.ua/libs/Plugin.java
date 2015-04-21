package com.niklabs.perfectplayer.plugin;

import java.util.Properties;

/**
 *  Perfect Player plugin interface.
 *
 *  icon.png - plugin icon image
 *  config.ini - plugin properties
 *  
 *  @author Sergey Nikitin
 *  @version 0.3.0 (code 3)
 */
public interface Plugin {
    static final public int LINK_TYPE_FILE = 1;
    static final public int LINK_TYPE_FOLDER = 2;

    /**
     * @return plugin display name
     */
    public String getPluginName();

    /**
     * @return plugin version code
     */
    public int getPluginVersionCode();

    /**
     * Init plugin using properties
     */
    public void init(Properties properties);

    /**
     * Set plugin icon image
     */
    public void setPluginIcon(Object icon);

    /**
     * @return plugin icon image
     */
    public Object getPluginIcon();

    /**
     * Download and parse current site's URL.
     * Prepare internal data structures (names, URLs, types, descriptions, thumbs).
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
     * @return current page thumbs URLs
     */
    public String[] getThumbs();

    /**
     * @return current page links types (video links or folders)
     */
    public int[] getTypes();

    /**
     * @return current page itemNum's link description (extra data)
     */
    public String getDescription(int itemNum);

    /**
     * @return true if plugin provides extra data (thumbs, descriptions), otherwise false
     */
    public boolean isProvideExtraData();

    /**
     * Enter to itemNum folder in current page.
     * Method should call {@link #refresh()} in the end.
     * @return true if successfully entered folder, otherwise false
     */
    public boolean selectItem(int itemNum);

    /**
     * Go to previous page.
     * Method should call {@link #refresh()} in the end.
     * @return true if successfully went, otherwise false
     */
    public boolean previousPage();

    /**
     * Go to next page.
     * Method should call {@link #refresh()} in the end.
     * @return true if successfully went, otherwise false
     */
    public boolean nextPage();

    /**
     * Enter to filesLink.
     * If filesLin is null, then plugin will return to previous safe state
     * filesLink is String array of 4 elements:
     * array[0] is current files parent folder name or itemNum's folder name
     * array[1] is current files parent folder URL or itemNum's folder URL
     * array[2] is current files parent folder description or itemNum's folder description
     * array[3] is current files parent folder thumb URL or itemNum's folder thumb URL
     * Method should call {@link #refresh()} in the end.
     * @return true if successfully entered folder, otherwise false
     */
    public boolean setFilesLink(String[] filesLink);

    /**
     * @return String array of 4 elements:
     * array[0] is current files parent folder name or itemNum's folder name
     * array[1] is current files parent folder URL or itemNum's folder URL
     * array[2] is current files parent folder description or itemNum's folder description
     * array[3] is current files parent folder thumb URL or itemNum's folder thumb URL
     */
    public String[] getFilesLink(int itemNum);

}
