import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import player.plugin.DataObtainedListener;
import player.plugin.Plugin;

/**
 * Plugin 'ex.ua' for the Perfect Player
 * @version 0.1.05
 */
public class ExUa implements Plugin {
	final static private boolean USE_PROXY = false;
	
	private String baseURLStr = "http://www.ex.ua";
	private String currUrlStr = null;
	private ArrayList<String> alURLsHistory = new ArrayList<String>();
	
	private String pageText = "";
	
	private String[] names = null;
	private String[] urls = null;
	private boolean[] types = null;
	private String[] descriptions = null;
	private String[] thumbsURLs = null;
	
	private ThumbObtainer thumbObtainer = null;
	
	private class ThumbObtainer extends Thread {
		private String thumbURL = null;
		private BufferedImage thumb = null;
		private DataObtainedListener dataObtainedListener = null;		
		private boolean needCancel = false;
		
		public ThumbObtainer(String thumbURL, DataObtainedListener dataObtainedListener) {
			if (thumbURL == null || dataObtainedListener == null) return;
			
			this.thumbURL = thumbURL;
			this.dataObtainedListener = dataObtainedListener; 
		}
		
		public void cancel() {
			needCancel = true;
		}
		
		public boolean checkSameThumbPreviouslyLoaded(String newThumbURL, DataObtainedListener dataObtainedListener) {
			if (dataObtainedListener != null && thumb != null && thumbURL.equals(newThumbURL)) {
				dataObtainedListener.thumbObtained(thumb);
				return true;
			} else return false;			
		}
		
		@Override
		public void run() {
			try {
				thumb = ImageIO.read(new URL(thumbURL));
				if (needCancel) return;
				dataObtainedListener.thumbObtained(thumb);
			} catch (IOException e) {
				System.out.println("Error loading thumb image from URL: " + thumbURL);
				return;
			}
		}
	}
	
	public ExUa() {
		String lang = "ru";
		try {
			Properties props = new Properties();
			props.load(getClass().getResourceAsStream("ExUa.ini"));			
			lang = props.getProperty("Language");
			if (lang == null || lang.length() != 2) lang = "ru";
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		currUrlStr = baseURLStr + "/" + lang + "/video?p=0";
	}
	
	private void downloadPage() throws IOException {
		BufferedReader in = null;
		pageText = "";
		try {
			URL url = new URL(currUrlStr);
			HttpURLConnection urlConn = null;
			
			if (USE_PROXY) urlConn = (HttpURLConnection)url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.65.113", 3128)));
    		else urlConn = (HttpURLConnection)url.openConnection();
			
			in = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));
			
			String line = null;			
			while ((line = in.readLine()) != null) {
				pageText += line;
			}
		} finally {
			if (in != null) in.close();
		}
	}
	
	private void parsePage() {
		Pattern pattern = null;
		Matcher matcher = null;
		ArrayList<String> alNames = null;
		ArrayList<String> alURLs = null;
		
		// Try to find Videos
		names = null;
		urls = null;
		types = null;
		
		pattern = Pattern.compile("</span><br><a href=.+?</a>", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(pageText);
		alNames = new ArrayList<String>();
		alURLs = new ArrayList<String>();
		while (matcher.find()) {
			try {
				String video = pageText.substring(matcher.start(), matcher.end());
				if (video.toLowerCase().indexOf(".avi") == -1 && video.toLowerCase().indexOf(".mkv") == -1 && video.toLowerCase().indexOf(".ts") == -1 &&
					video.toLowerCase().indexOf(".m2ts") == -1 && video.toLowerCase().indexOf(".mp4") == -1 && video.toLowerCase().indexOf(".m4v") == -1 &&
					video.toLowerCase().indexOf(".flv") == -1 && video.toLowerCase().indexOf(".vob") == -1 && video.toLowerCase().indexOf(".mpg") == -1 &&
					video.toLowerCase().indexOf(".mpeg") == -1 && video.toLowerCase().indexOf(".mov") == -1 && video.toLowerCase().indexOf(".wmv") == -1) {
						continue;
				}
				String url = baseURLStr + video.substring(video.indexOf("'") + 1, video.indexOf("' "));
				String name = null;
				try {
					name = video.substring(video.indexOf("title='") + 7, video.indexOf("'", video.indexOf("title='") + 7)).
						replaceAll("&amp;", "&").replaceAll("&#39;", "'").replaceAll("&quot;", "\"");
				} catch (Exception e) {
					name = video.substring(video.indexOf(">", 19) + 1, video.indexOf("<", 19)).
						replaceAll("&amp;", "&").replaceAll("&#39;", "'").replaceAll("&quot;", "\"");
				}
				alNames.add(name);
				alURLs.add(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (alNames.size() > 0) {
			names = alNames.toArray(new String[0]);
			urls = alURLs.toArray(new String[0]);
			types = new boolean[names.length];
			for (int i = 0;i < types.length;i++) types[i] = false; // false for Videos
			
			// Getting Description and Thumb
			pattern = Pattern.compile("valign=top>.+?</td>", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(pageText);
			if (matcher.find()) {
				try {
					String description = pageText.substring(matcher.start(), matcher.end());				 
					description = description.substring(11);
					description = description.replaceAll("<br>", "\n");
					description = description.replaceAll("&nbsp;", "\n");
					description = description.replaceAll("<.*?>", "");
					descriptions = new String[urls.length];
					for (int i = 0;i < urls.length;i++) descriptions[i] = description;
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					String thumb = pageText.substring(matcher.start(), matcher.end());
					thumb = thumb.substring(thumb.indexOf("<img src='") + 10, thumb.indexOf("'", thumb.indexOf("<img src='") + 10));
					System.out.println(thumb);
					thumbsURLs = new String[urls.length];
					for (int i = 0;i < urls.length;i++) thumbsURLs[i] = thumb;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		} else {
			// If Videos not found then try to find Folders
			names = null;
			urls = null;
			types = null;
			
			pattern = Pattern.compile("valign=center><a href=.+?</b>", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(pageText);
			alNames = new ArrayList<String>();
			alURLs = new ArrayList<String>();
			while (matcher.find()) {
				try {
					String category = pageText.substring(matcher.start(), matcher.end());				
					String url = baseURLStr + category.substring(category.indexOf("'") + 1, category.indexOf("'", category.indexOf("'") + 1)) + "&p=0";
					String name = category.substring(category.toLowerCase().indexOf("<b>") + 3, category.toLowerCase().indexOf("</b>")).
						replaceAll("&amp;", "&").replaceAll("&#39;", "'").replaceAll("&quot;", "\"");
					alNames.add(name);
					alURLs.add(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (alNames.size() > 0) {
				names = alNames.toArray(new String[0]);
				urls = alURLs.toArray(new String[0]);
				types = new boolean[names.length];
				for (int i = 0;i < types.length;i++) types[i] = true; // true for Folders
			}
		}
	}
	
	@Override
	public boolean refresh() {
		try {
			downloadPage();
			parsePage();
			return true;
		} catch (Exception e) {
			System.err.println("Error reading from URL: " + currUrlStr);
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String[] getNames() {
		return names;
	}
	
	@Override
	public String[] getURLs() {
		return urls;
	}
	
	@Override
	public boolean[] getTypes() {
		return types;
	}
	
	@Override
	public boolean isProvideExtraData() {
		return true;
	}
	
	@Override
	public String getDescription(int itemNum) {
		if (descriptions == null || descriptions.length <= itemNum) return null;
		return descriptions[itemNum];
	}
	
	@Override
	public void requestThumb(int itemNum, DataObtainedListener dataObtainedListener) {
		if (thumbsURLs == null || thumbsURLs.length <= itemNum) return;
		
		if (thumbObtainer != null && !thumbObtainer.isAlive() &&
			thumbObtainer.checkSameThumbPreviouslyLoaded(thumbsURLs[itemNum], dataObtainedListener)) return;
		
		if (thumbObtainer != null && thumbObtainer.isAlive()) thumbObtainer.cancel();		
		thumbObtainer = new ThumbObtainer(thumbsURLs[itemNum], dataObtainedListener);
		thumbObtainer.start();
	}

	@Override
	public String getPluginName() {
		return "ex.ua";
	}

	@Override
	public boolean nextPage() {
		if (urls == null || urls.length == 0 || !types[0]) return false;
		
		try {
			int pageNum = 0;
			pageNum = Integer.parseInt(currUrlStr.substring(currUrlStr.lastIndexOf("=") + 1));
			alURLsHistory.add(currUrlStr);
			currUrlStr = currUrlStr.substring(0, currUrlStr.lastIndexOf("=") + 1) + (pageNum + 1);
			
			boolean res = refresh();
			if (res && urls == null) {
				previousPage();
				return false;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean previousPage() {
		if (alURLsHistory.size() == 0) return false;
		
		currUrlStr = alURLsHistory.get(alURLsHistory.size() - 1);
		alURLsHistory.remove(alURLsHistory.size() - 1);
		
		return refresh();
	}

	@Override
	public void selectItem(int itemNum) {
		if (urls == null || urls.length <= itemNum || types == null || !types[itemNum]) return;
		
		alURLsHistory.add(currUrlStr);
		currUrlStr = urls[itemNum];
		
		refresh();
	}
	
	// Just for plugin local testing
	private void testPlugin() {		
		System.out.println("--- " + getPluginName() + " ---");
		System.out.println("Refresh status: " + refresh());
		if (names != null) {
			for (int i = 0;i < names.length;i++)
				System.out.println(names[i] + " - " + urls[i] + " - " + types[i]);
			
			selectItem(2);
			if (names != null) {
				for (int i = 0;i < names.length;i++)
					System.out.println(names[i] + " - " + urls[i] + " - " + types[i]);
				
				selectItem(0);
				if (names != null) {
					for (int i = 0;i < names.length;i++)
						System.out.println(names[i] + " - " + urls[i] + " - " + types[i]);
				}
			}
		}
	}
	
	// Just for plugin local testing
	public static void main(String[] args) {
		ExUa exUa = new ExUa();
		exUa.testPlugin();
	}
	
}
