package com.tabnine.eclipse.module.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.tabnine.eclipse.data.TabNineCore;
import com.tabnine.eclipse.enums.TabNinePlatformInfo;
import com.tabnine.eclipse.exception.TabNineApplicationException;
import com.tabnine.eclipse.module.TabNineCoreDownloader;
import com.tabnine.eclipse.module.TabNineCoreManager;
import com.tabnine.eclipse.util.TabNineLangUtils;

/**
 * The basic implementation of {@link TabNineCoreManager}
 * @author ZhouYi
 * @version version
 * @date 2019-10-30 10:35:11
 * @description description
 */
public class TabNineCoreManagerBasicImpl implements TabNineCoreManager {

	// ===== ===== ===== ===== [Constants] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Variables] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Entry Method (For test only)] ===== ===== ===== ===== //
	
	public static void main(String[] args) {
		System.out.println(getLocalTabNineCoreList(TABINE_CORE_ROOT_FOLDER_PATH, TabNinePlatformInfo.getTabNinePlatformInfoMatchedCurrentOS()));
		
	}
	
	// ===== ===== ===== ===== [Test Methods] ===== ===== ===== ===== //

	
	// ===== ===== ===== ===== [Instance Variables] ===== ===== ===== ===== //
	
	/** The tabNineCore kept by this manager : {TabNineCore} tabNinecore */
	protected TabNineCore tabNineCore = null;
	
	/** The downloader to get latest TabNine core from remote : {tabNineCoreDownloaderBasicImpl} tabNineCoreDownloader */
	protected TabNineCoreDownloader tabNineCoreDownloader = new TabNineCoreDownloaderBasicImpl();
	
	// ===== ===== ===== ===== [Instance Methods] ===== ===== ===== ===== //
	
	/* (non-Javadoc)
	 * @see com.tabnine.eclipse.module.impl.TabNineCoreManager#loadTabNineCore()
	 * @return
	 * @writer ZhouYi
	 * @date 2019-11-07 17:00:43
	 * @description description
	 */
	@Override
	public TabNineCore loadTabNineCore() {
		// STEP Number Check if the {@link #tabNineCore} was initialized
		if (this.tabNineCore == null) {
			initializeTabNineCore(false);
			
		}
		
		// STEP Number Return the {@link #tabNineCore}
		return this.tabNineCore;
		
	}
	
	// ===== ===== ===== ===== [Instance Utility Methods - Utility] ===== ===== ===== ===== //

	/**
	 * Initialize the {@link #tabNineCore} of this manager
	 * @param forcibly If the {@link #tabNineCore} already existed, should this function init and cover it
	 * @author ZhouYi
	 * @since version
	 * @date 2019-11-01 10:23:33
	 * @description description
	 */
	protected void initializeTabNineCore(boolean forcibly) {
		// STEP Number Declare the log variables
		String logTitle = "Try to initialize TabNine core"; // Log message title
		String logMessage = null; // Log message text
		
		// STEP Number Check if the {@link #tabNineCore} was initialized
		if ((this.tabNineCore != null) && !forcibly) {
			return;
			
		}
		
		// STEP Number Get core from local storage
		TabNinePlatformInfo platformInfo = TabNinePlatformInfo.getTabNinePlatformInfoMatchedCurrentOS();
		List<TabNineCore> localTabNineCoreList = getLocalTabNineCoreList(
				TABINE_CORE_ROOT_FOLDER_PATH
				, platformInfo
		);
		
		// STEP Number Check local core list
		// BRANCH Number If the local core list is empty
		if (localTabNineCoreList.isEmpty()) {
			// SUBSTEP Number Print log
			System.err.println(logTitle + " - Cannot find any TabNine core in local storage, try to download a latest one from remote");
			
			// SUBSTEP Number Make up the information of latest TabNine Core
			String latestVersion = this.tabNineCoreDownloader.getLatestTabNineVersion();
			String downloadUrlPath = this.tabNineCoreDownloader.getDownloadUrlPath(latestVersion);
			String destFilePath = generateLocalTabNineCoreFilePath(
					TABINE_CORE_ROOT_FOLDER_PATH
					, latestVersion
					, platformInfo.toPlatformTypeText()
					, TABNINE_CORE_FILE_NAME_LOCAL + platformInfo.getSuffixNameOfExecutableFile()
			);
			
			// SUBSTEP Number Download File (blocking operation)
			File downloadedCoreFile = this.tabNineCoreDownloader.downloadTabNineCode(downloadUrlPath, destFilePath);
			
			// SUBSTEP Number Check File and init
			if (isTabNineCoreAvailable(downloadedCoreFile)) {
				this.tabNineCore = new TabNineCore(downloadedCoreFile, latestVersion, platformInfo, false, null);
				return;
				
			} else {
				logMessage = logTitle + " - Failed: The local TabNine core dose not exist, and downloading operation is failed neither";
				System.err.println(logMessage);
				throw new TabNineApplicationException(logMessage);
				
			}
			
		// BRANCH Number If the local core list is not empty
		} else {
			// SUBSTEP Number Get the newest core among local core list
			sortTabNineCoreByVersion(localTabNineCoreList, true);
			TabNineCore newestCore = localTabNineCoreList.get(0);
			
			// SUBSTEP Number Start a new thread to find available update for TabNine Core
			new Thread(new Runnable() {

				@Override
				public void run() {
					// PART Number Get the latest version from remote
					String latestVersion = tabNineCoreDownloader.getLatestTabNineVersion();
					
					// PART Number Compare the two versions, check if there is a newer version
					int compareResult = safelyCompareTabNineVersion(newestCore.getVersion(), latestVersion);
					
					// PART Number Print log
					System.out.println(logTitle + " - Version Check: A latest version is [" + latestVersion + "], the newest version in local storage is [" + newestCore.getVersion() + "]");
					
					// SUBSTEP Number If a update was found, download it
					if (compareResult < 0) {
						// INNER-PART Number Print log
						System.out.println(logTitle + " - Try to download the latest version [" + latestVersion + "] core asynchronously");
						
						// INNER-PART Number Make up the information of latest TabNine Core
						String downloadUrlPath = tabNineCoreDownloader.getDownloadUrlPath(latestVersion);
						String destFilePath = generateLocalTabNineCoreFilePath(
								TABINE_CORE_ROOT_FOLDER_PATH
								, latestVersion
								, platformInfo.toPlatformTypeText()
								, TABNINE_CORE_FILE_NAME_LOCAL + platformInfo.getSuffixNameOfExecutableFile()
						);
						
						// INNER-PART Number Download File (blocking operation)
						tabNineCoreDownloader.downloadTabNineCode(downloadUrlPath, destFilePath);
					
						// INNER-PART Number Get core from local storage
						List<TabNineCore> localTabNineCoreList = getLocalTabNineCoreList(
								TABINE_CORE_ROOT_FOLDER_PATH
								, platformInfo
						);
						
						// INNER-PART Number Check the amount of core file
						if (localTabNineCoreList.size() > LOCAL_TABNINE_CORE_FILE_LIMIT) {
							sortTabNineCoreByVersion(localTabNineCoreList, false);
							localTabNineCoreList.get(0).getCoreFile().deleteOnExit();
							
						}
						
					}
					
				}
				
			}).start();
			
			// SUBSTEP Number Set the newest core in local storage as current core
			this.tabNineCore = newestCore;
			
		}
		
	}
	
	// ===== ===== ===== ===== [Static Utility Methods - Utility] ===== ===== ===== ===== //
	
	/**
	 * Get local TabNine core list from specified folder
	 * @param rootFolderPath The root folder of TabNine core storage
	 * @param tabNinePlatformInfo The platform which TabNine core is corresponding to
	 * @return coreList The TabNine core list got
	 * @author ZhouYi
	 * @since version
	 * @date 2019-11-01 10:48:20
	 * @description description
	 */
	protected static List<TabNineCore> getLocalTabNineCoreList(String rootFolderPath, TabNinePlatformInfo tabNinePlatformInfo) {
		// STEP Number Declare the log variables
		String logTitle = "Try to get local TabNine core list"; // Log message title
		String logMessage = null; // Log message text
		
		// STEP Number Validate incoming parameters
		if (TabNineLangUtils.isBlank(rootFolderPath) || (tabNinePlatformInfo == null)) {
			logMessage = logTitle + " - Failed: the folder information is not complete";
			System.err.println(logMessage);
			throw new TabNineApplicationException(logMessage);
			
		}
		
		// STEP Number Check if the container folder existed
		File folder = new File(rootFolderPath);
		if (!folder.isDirectory()) {
			logMessage = logTitle + " - Failed: the folder [" + rootFolderPath + "] dose not exist or was not a directory";
			System.err.println(logMessage);
			throw new TabNineApplicationException(logMessage);
			
		}
		
		// STEP Number Declare relative variables
		List<TabNineCore> coreList = new ArrayList<TabNineCore>(); // The core list got from local
		File[] versionFolderArray = folder.listFiles(); // The version folders in platform folder
		String platformTypeText = tabNinePlatformInfo.toPlatformTypeText();
		String coreFileName = TABNINE_CORE_FILE_NAME_LOCAL + tabNinePlatformInfo.getSuffixNameOfExecutableFile(); // The name of core file in target platform
		
		// STEP Number Traverse all the core file inside each version folder
		outerForLoop : for (File versionFolder : versionFolderArray) {
			// SUBSTEP Number Check the version folder
			if (!versionFolder.isDirectory()) {
				continue;
				
			}
			
			// SUBSTEP Number Concatenate core file path
			String coreFilePath = generateLocalTabNineCoreFilePath(
					TABINE_CORE_ROOT_FOLDER_PATH
					, versionFolder.getName()
					, platformTypeText
					, coreFileName
			);
			
			// SUBSTEP Number Try to get the core file
			File coreFile = new File(coreFilePath);
			if (coreFile.isFile() && isTabNineCoreAvailable(coreFile)) {
				coreList.add(new TabNineCore(coreFile, versionFolder.getName(), tabNinePlatformInfo, false, null));
				continue outerForLoop;
				
			}
			
		}
		
		// STEP Number Return the core file got
		return coreList;
		
	}
	
	/**
	 * Generate path text for local TabNine core file
	 * @param rootFolderPath The path of root folder contains TabNine core
	 * @param version The version of target TabNine core
	 * @param platformTypeText The platform type of target TabNine core
	 * @param coreFileName The name of target TabNine core
	 * @return localTabNineCoreFilePath The path generated
	 * @author ZhouYi
	 * @date 2019-11-01 12:31:10
	 * @description description
	 * @note note
	 */
	protected static String generateLocalTabNineCoreFilePath(String rootFolderPath, String version, String platformTypeText, String coreFileName) {
		// STEP Number Concatenate and return result
		return rootFolderPath + File.separator 
				+ version + File.separator 
				+ platformTypeText + File.separator 
				+ coreFileName;
		
	}
	
	/**
	 * Is target TabNine core file complete and available
	 * @param coreFile The core file to judge
	 * @return isAvailalle The judgment result
	 * @author ZhouYi
	 * @date 2019-11-01 12:19:27
	 * @description description
	 * @note note
	 */
	protected static boolean isTabNineCoreAvailable(File coreFile) {
		// TODO Number Find the way to validate the availability of TabNine core file
		return true;
		
	}
	
	/**
	 * Sort the TabNine core object in list by their version
	 * @param coreList The TabNine core list
	 * @param desc Is the sort order desc
	 * @author ZhouYi
	 * @date 2019-11-01 12:59:27
	 * @description description
	 * @note note
	 */
	protected static void sortTabNineCoreByVersion(List<TabNineCore> coreList, boolean desc) {
		coreList.sort(new Comparator<TabNineCore>() {

			@Override
			public int compare(TabNineCore core1, TabNineCore core2) {
				// STEP Number Make comparsion
				int comparisonResult = safelyCompareTabNineVersion(core1.getVersion(), core2.getVersion());
				
				// STEP Number Return 0 (for insurability)
				return desc ? - comparisonResult : comparisonResult;
				
			}
			
		});
		
	}
	
	/**
	 * Safely parse and compare two version text of TabNine core
	 * @param version1 The version text 1
	 * @param version2 The version text 2
	 * @return comparisonRestul {@link Integer#compare(int, int)}
	 * @author ZhouYi
	 * @date 2019-11-01 13:12:48
	 * @description description
	 * @note This method suit only the simple version format like "2.1.17", if there were any hyphen or modifier, it will not be recognized
	 * @note note
	 */
	protected static int safelyCompareTabNineVersion(String version1, String version2) {
		// STEP Number Check if the version text is blank
		boolean isVersion1Blank = TabNineLangUtils.isBlank(version1);
		boolean isVersion2Blank = TabNineLangUtils.isBlank(version2);
		
		// STEP Number Make judgment if there was at least one blank version
		if (isVersion1Blank && isVersion2Blank) {
			return 0;
			
		} else if (isVersion1Blank) {
			return -1;
			
		} else if (isVersion2Blank) {
			return 1;
			
		}
		
		// STEP Number Make judgment when the two version text were both available
		String[] versionPart1 = version1.split("\\.");
		String[] versionPart2 = version2.split("\\.");
		int comparisonResult = 0;
		int versionLength = (versionPart1.length < versionPart2.length) ? versionPart1.length : versionPart2.length;
		for (int i = 0; i < versionLength; i++) {
			comparisonResult = safelyCompareIntegerText(versionPart1[i], versionPart2[i]);
			if (comparisonResult != 0) {
				return comparisonResult;
				
			}
			
		}
		
		// STEP Number Return 0 (for insurability)
		return comparisonResult;
		
	}
	
	/**
	 * Safely parse and compare two integer text
	 * @param intText1 The integer text 1
	 * @param intText2 The integer text 2
	 * @return comparisonRestul {@link Integer#compare(int, int)}
	 * @author ZhouYi
	 * @date 2019-11-01 12:54:34
	 * @description description
	 * @note note
	 */
	protected static int safelyCompareIntegerText(String intText1, String intText2) {
		// STEP Number Declare variables and set default value
		Integer int1 = Integer.MIN_VALUE;
		Integer int2 = Integer.MIN_VALUE;
		
		// STEP Number Try to parse int, if failed, it remains default value
		try {
			int1 = Integer.parseInt(intText1);
		} catch (NumberFormatException e) {
			System.err.println("Error happened while parsing text [" + intText1 + "] to int.");
		}
		try {
			int2 = Integer.parseInt(intText2);
		} catch (NumberFormatException e) {
			System.err.println("Error happened while parsing text [" + intText2 + "] to int.");
		}
		
		// STEP Number Return comparison result
		return Integer.compare(int1, int2);
		
	}
	
	// ===== ===== ===== ===== [Constructors] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Factory Methods] ===== ===== ===== ===== //
	
}