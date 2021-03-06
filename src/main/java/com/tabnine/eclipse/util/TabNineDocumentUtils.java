package com.tabnine.eclipse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tabnine.eclipse.data.AutocompleteArgs;
import com.tabnine.eclipse.data.AutocompleteResponse;
import com.tabnine.eclipse.data.IContextRange;
import com.tabnine.eclipse.data.IContextRangeComputer;
import com.tabnine.eclipse.data.ResultEntry;

/**
 * The DocumentUtils used in this project (The "TabNine" prefix is just for distinguishing)
 * @author ZhouYi
 * @date 2019-11-15 10:58:47
 * @description description
 * @note note
 */
public class TabNineDocumentUtils {
	
	// ===== ===== ===== ===== [Constants] ===== ===== ===== ===== //

	/** Get the before/after text from current line : int FROM_LINE
	 * @note In this mode, the proposals from TabNine will be a little bit more confined
	 * @note note
	 */
	public static final int FROM_LINE = 0;
	
	/** Get the before/after text from the whole document : int FROM_DOC
	 * @note In this mode, the proposals from TabNine will be much more as well as the cost grows 
	 * @note note 
	 */
	public static final int FROM_DOC = 1;
	
	/** Get the before/after text from the custom range computer : int FROM_RANGE_COMPUTER */
	public static final int FROM_RANGE_COMPUTER = 2;
	
	/** An empty proposal list to return when no proposal got : List<ICompletionProposal> EMPTY_PROPOSAL_LIST */
	public static final List<ICompletionProposal> EMPTY_PROPOSAL_LIST = new ArrayList<ICompletionProposal>();

	/** The probable paths of TabNine logo image  : List<String> TABNINE_LOGO_IMAGE_PATHS */
	public static final List<String> TABNINE_LOGO_IMAGE_PATHS = Arrays.asList(
			"icon/tabnine-logo-16x16.png"
			, "src/main/resources/icon/tabnine-logo-16x16.png"
	);
	
	// ===== ===== ===== ===== [Static Variables] ===== ===== ===== ===== //
	
	/** The SWT {@link Image} object of TabNine logo picture : Image tabNineLogoImage */
	public static Image tabNineLogoImage = null;
	
	// ===== ===== ===== ===== [Entry Method (For test only)] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Test Methods] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Utility Methods - AutocompleteArgs relative] ===== ===== ===== ===== //
	
	/**
	 * Extract the arguments information for TabNine auto-complete from {@link ContentAssistInvocationContext} (JavaScript Context)
	 * @param context The {@link ContentAssistInvocationContext} object which this method is working on
	 * @param extractionRange The range we extract before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param rangeComputer A {@link IContextRangeComputer} object to compute the before/after range from context
	 * @return autocompleteArgs The arguments extracted 
	 * @author ZhouYi
	 * @date 2019-12-01 17:42:05
	 * @description description
	 * @note note
	 */
	public static AutocompleteArgs extractAutocompleteArgs(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext context, int extractionRange, IContextRangeComputer rangeComputer) {
		// STEP Number Validate incoming parameters
		if (context == null) {
			return null;
			
		}
		
		// STEP Number Invoke relative method and return result
		return extractAutocompleteArgs(
				context.getInvocationOffset()
				, context.getDocument()
				, context.getViewer()
				, extractionRange
				, rangeComputer
		);
		
	}
	
	/**
	 * Extract the arguments information for TabNine auto-complete from {@link ContentAssistInvocationContext} (Java Context)
	 * @param context The {@link ContentAssistInvocationContext} object which this method is working on
	 * @param extractionRange The range we extract before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param rangeComputer A {@link IContextRangeComputer} object to compute the before/after range from context
	 * @return autocompleteArgs The arguments extracted 
	 * @author ZhouYi
	 * @date 2019-12-01 17:42:05
	 * @description description
	 * @note note
	 */
	public static AutocompleteArgs extractAutocompleteArgs(ContentAssistInvocationContext context, int extractionRange, IContextRangeComputer rangeComputer) {
		// STEP Number Validate incoming parameters
		if (context == null) {
			return null;
			
		}
		
		// STEP Number Invoke relative method and return result
		return extractAutocompleteArgs(
				context.getInvocationOffset()
				, context.getDocument()
				, context.getViewer()
				, extractionRange
				, rangeComputer
		);
		
	}
	
	/**
	 * Extract the arguments information for TabNine auto-complete from {@link IDocument} and {@link ITextViewer}
	 * @param invocationOffset The offset position in the text which this method is invoked (from context)
	 * @param document The {@link IDocument} target of current edited (from context)
	 * @param viewer The {@link ITextViewer} target of current edited (from context)
	 * @param extractionRange The range we extract before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param rangeComputer A {@link IContextRangeComputer} object to compute the before/after range from context
	 * @return autocompleteArgs The arguments extracted 
	 * @author ZhouYi
	 * @date 2019-12-01 17:42:05
	 * @description description
	 * @note note
	 */
	public static AutocompleteArgs extractAutocompleteArgs(int invocationOffset, IDocument document, ITextViewer viewer
			, int extractionRange, IContextRangeComputer rangeComputer) {
		// STEP Number Declare the log variables
		String logTitle = "Try to extract arguments information for TabNine auto-complete request from document"; // Log message title
		String logMessage = null; // Log message text
		
		// STEP Number Validate incoming parameters
		if ((invocationOffset < 0) || (document == null)) {
			return null;
			
		}
		
		// STEP Number Declare argument variables
		AutocompleteArgs autocompleteArgs = null;
		
		// STEP Number Fill argument object by different ways according to the method settings
		try {
			// BRANCH Number If get before/after text from the whole text
			if (extractionRange == FROM_DOC) {
				autocompleteArgs = new AutocompleteArgs();
				autocompleteArgs.setBefore(document.get(0, invocationOffset));
				autocompleteArgs.setAfter(document.get(invocationOffset, document.getLength() - invocationOffset));
				autocompleteArgs.setRegionIncludesBeginning(true);
				autocompleteArgs.setRegionIncludesEnd(true);
				
				// BRANCH Number If get before/after text from current line
			} else if (extractionRange == FROM_LINE) {
				IRegion lineInformation = document.getLineInformationOfOffset(invocationOffset);
				int lineHeadOffset = lineInformation.getOffset(); // The position of current line head in the whole text
				int lineLength = lineInformation.getLength(); // The length of current line
				int totalLineNumber = document.getNumberOfLines(); // The total line count of this document
				int currentLineNumber = document.getLineOfOffset(lineHeadOffset) + 1; // The number of current line (index of line plus one)
				autocompleteArgs = new AutocompleteArgs();
				autocompleteArgs.setBefore(document.get(lineHeadOffset, invocationOffset - lineHeadOffset));
				autocompleteArgs.setAfter(document.get(invocationOffset, lineLength - (invocationOffset - lineHeadOffset)));
				autocompleteArgs.setRegionIncludesBeginning((currentLineNumber == 1));
				autocompleteArgs.setRegionIncludesEnd((currentLineNumber == totalLineNumber));
				
				// BRANCH Number If get before/after text from specified range computer
			} else if (extractionRange == FROM_RANGE_COMPUTER) {
				if (rangeComputer != null) {
					IContextRange range = rangeComputer.computeRange(invocationOffset, document, viewer);
					autocompleteArgs = new AutocompleteArgs();
					autocompleteArgs.setBefore(document.get(range.getHeadOffset(), invocationOffset));
					autocompleteArgs.setAfter(document.get(invocationOffset, range.getTailOffset()));
					autocompleteArgs.setRegionIncludesBeginning(range.isRangeIncludesBeginingOfFile());
					autocompleteArgs.setRegionIncludesEnd(range.isRangeIncludesEndingOfFile());
					
				}
				
			}
			
		} catch (BadLocationException e) {
			logMessage = logTitle + " - Failed: Unknown BadLocationException hanppend.";
			TabNineLoggingUtils.error(logMessage, e);
			return null;
			
		}
		
		// STEP Number Return the object created
		return autocompleteArgs;
		
	}
	
	// ===== ===== ===== ===== [Static Utility Methods - ICompletionProposal relative] ===== ===== ===== ===== //
	
	/**
	 * Generate {@ICompletionProposal} list using the response information from TabNine auto-complete application (JavaScript Context)
	 * @param context The {@link ContentAssistInvocationContext} object which this method is working on
	 * @param response The response from TabNine auto-complete application
	 * @param extractionRange The range we got before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param isHTMLStyleSupported Did these proposals support the HTML style decoration like {@code "<del>Deprecated</del>"}
	 * @return proposalList The proposal list generated
	 * @author ZhouYi
	 * @date 2019-12-01 18:08:45
	 * @description description
	 * @note note
	 */
	public static List<ICompletionProposal> generateCompletionProposal(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext context, AutocompleteResponse response
			, int extractionRange, boolean isHTMLStyleSupported) {
		// STEP Number Validate incoming parameters
		if (context == null) {
			return EMPTY_PROPOSAL_LIST;
			
		}
		
		// STEP Number Invoke relative method and return result
		return generateCompletionProposal(
				context.getInvocationOffset()
				, context.getDocument()
				, context.getViewer()
				, response
				, extractionRange
				, isHTMLStyleSupported
		);
		
	}
	
	/**
	 * Generate {@ICompletionProposal} list using the response information from TabNine auto-complete application (Java Context)
	 * @param context The {@link ContentAssistInvocationContext} object which this method is working on
	 * @param response The response from TabNine auto-complete application
	 * @param extractionRange The range we got before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param isHTMLStyleSupported Did these proposals support the HTML style decoration like {@code "<del>Deprecated</del>"}
	 * @return proposalList The proposal list generated
	 * @author ZhouYi
	 * @date 2019-12-01 18:08:45
	 * @description description
	 * @note note
	 */
	public static List<ICompletionProposal> generateCompletionProposal(ContentAssistInvocationContext context, AutocompleteResponse response
			, int extractionRange, boolean isHTMLStyleSupported) {
		// STEP Number Validate incoming parameters
		if (context == null) {
			return EMPTY_PROPOSAL_LIST;
			
		}
		
		// STEP Number Invoke relative method and return result
		return generateCompletionProposal(
				context.getInvocationOffset()
				, context.getDocument()
				, context.getViewer()
				, response
				, extractionRange
				, isHTMLStyleSupported
		);
		
	}
	
	/**
	 * Generate {@ICompletionProposal} list using the response information from TabNine auto-complete application
	 * @param invocationOffset The offset position in the text which this method is invoked (from context)
	 * @param document The {@link IDocument} target of current edited (from context)
	 * @param viewer The {@link ITextViewer} target of current edited (from context)
	 * @param response The response from TabNine auto-complete application
	 * @param extractionRange The range we got before/after text from context, includes: 
	 *     <li>{@link #FROM_LINE}: Get the before/after text from current line</li>
	 *     <li>{@link #FROM_DOC}: Get the before/after text from the whole documents(not recommend)</li>
	 *     <li>{@link #FROM_RANGE_COMPUTER}: Get the before/after text by a custom range computer(need to provide a computer object)</li>
	 * @param isHTMLStyleSupported Did these proposals support the HTML style decoration like {@code "<del>Deprecated</del>"}
	 * @return proposalList The proposal list generated
	 * @author ZhouYi
	 * @date 2019-12-01 18:08:45
	 * @description description
	 * @note note
	 */
	public static List<ICompletionProposal> generateCompletionProposal(int invocationOffset, IDocument document, ITextViewer viewer
			, AutocompleteResponse response, int extractionRange, boolean isHTMLStyleSupported) {
		// STEP Number Validate incoming parameters
		if ((invocationOffset < 0) || (response == null) || TabNineLangUtils.isEmpty(response.getResults())) {
			return EMPTY_PROPOSAL_LIST;
			
		}
		
		// STEP Number Get common information from response
		String oldPrefix = response.getOldPrefix();
		String userMessage = TabNineTextUtils.join(response.getUserMessage(), null);
		List<ResultEntry> results = response.getResults();
		
		// STEP Number Calculate additional information
		int oldPrefixLength = TabNineTextUtils.safelyGetLength(oldPrefix);
		boolean isUserMessageNotBlank = TabNineTextUtils.isNotBlank(userMessage);
		
		// STEP Number Declare the line break characters
		String lineBreak = isHTMLStyleSupported ? TabNineTextUtils.HTML_BREAK_ELEMENT : TabNineTextUtils.LINE_SEPARATOR;
		
		// STEP Number Handle the auto-complete result entities circularly
		List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>(results.size());
		for (ResultEntry resultEntry : results) {
			// SUBSTEP Number Read properties
			String newPrefix = resultEntry.getNewPrefix();
			String newSuffix = resultEntry.getNewSuffix();
			String oldSuffix = resultEntry.getOldSuffix();
			Boolean deprecated = resultEntry.getDeprecated();
			String detail = resultEntry.getDetail();
			String kind = resultEntry.getKind();
			String documentation = resultEntry.getDocumentation();
			
			// SUBSTEP Number Compute new text to replace the old one
			String newText = newPrefix.concat(newSuffix);
			
			// SUBSTEP Number Make up text
			StringBuilder infoBuilder = 
				isHTMLStyleSupported 
				? new StringBuilder("<font size=\"3\"><b>From TabNine</b></font>")
				: new StringBuilder("From TabNine");
			if (TabNineLangUtils.equalsWithNull(deprecated, true)) {
				infoBuilder.append(lineBreak);
				infoBuilder.append(
					isHTMLStyleSupported
					? "<del>Deprecated.</del>"
					: "Deprecated."
				);
				
			}
			if (TabNineTextUtils.isNotBlank(detail)) {
				infoBuilder.append(lineBreak).append(detail);
				
			}
			if (isUserMessageNotBlank) {
				infoBuilder.append(lineBreak);
				infoBuilder.append(
					isHTMLStyleSupported
					? "</font color=\"#D3D3D3\">".concat(userMessage).concat("</font>")
					: userMessage
				);
				
			}
			if (TabNineTextUtils.isNotBlank(kind)) {
				infoBuilder.append(lineBreak).append(kind);
				
			}
			if (TabNineTextUtils.isNotBlank(documentation)) {
				infoBuilder.append(lineBreak).append(documentation);
				
			}
			String additionalProposalInfo = infoBuilder.toString();
			
			// SUBSTEP Number Generate proposal object and add it to list
			// NOTE Number The argument of {@link org.eclipse.jface.text.contentassist.CompletionProposal} contains:
			//   String replacementString:                The new string we use to replace the old one.
			//   int replacementOffset:                   The start point we begin to replace.
			//   int replacementLength:                   The total length of old string which is needed to be replaced.
			//   int cursorPosition:                      The cursor position relative to replacement offset point after the replacement.
			//   Image image:                             The icon images displayed with proposal.
			//   String displayString:                    The entry text displayed for proposal.
			//   IContextInformation contextInformation:  The context information with this proposal.
			//   String additionalProposalInfo:           The additional proposal info text shown in detail dialog.
			proposalList.add(new CompletionProposal(
					newText
					, invocationOffset - oldPrefixLength
					, oldPrefixLength + TabNineTextUtils.safelyGetLength(oldSuffix)
					, TabNineTextUtils.safelyGetLength(newText)
					, loadTabNineLogoImage()
					, newText
					, null
					, additionalProposalInfo
			));
			
		}
		
		// STEP Number Return the proposal list we got
		return proposalList;
		
	}

	// ===== ===== ===== ===== [Static Utility Methods - Others] ===== ===== ===== ===== //
	
	/**
	 * Try to get the currently active editor part of eclipse, if failed, return null
	 * @return editor The editor we got
	 * @author ZhouYi
	 * @date 2020-01-06 13:55:07
	 * @description description
	 * @note note
	 */
	public static IEditorPart getAcitveEditor() {
		// STEP Number Try to get workbench
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			TabNineLoggingUtils.debug("Fail to get currently active editor of eclipse: the workbench has not been started");
			return null;
			
		}
		
		// STEP Number Try to get the active window
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			TabNineLoggingUtils.debug("Fail to get currently active editor of eclipse: there is no active window in current workbench");
			return null;
			
		}
		
		// STEP Number Try to get the active page
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			TabNineLoggingUtils.debug("Fail to get currently active editor of eclipse: there is no active page in current window");
			return null;
			
		}
		
		// STEP Number Try to get the active editor
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			TabNineLoggingUtils.debug("Fail to get currently active editor of eclipse: there is no active editor in current page");
			return null;
			
		}
		
		// STEP Number Return the result we got
		TabNineLoggingUtils.debug("Successfully get currently active editor of eclipse");
		return activeEditor;
		
	}
	
	/**
	 * Get the path of file which is currently edited
	 * @return path The path text got
	 * @author ZhouYi
	 * @date 2019-11-30 19:32:11
	 * @description description
	 * @note note
	 */
	public static String getPathOfCurrentlyEditingFile() {
		// STEP Number Get active editor and check
		IEditorPart activeEditor = getAcitveEditor();
		if (activeEditor == null) {
			return null;
			
		}
		
		// STEP Number Try to get the editor input
		IEditorInput editorInput = activeEditor.getEditorInput();
		if (editorInput == null) {
			return null;
			
		}
		
		// STEP Number Declare result variable
		String filePath = null;
		
		// NOTE Number The ways to get path of currently editing file is unstable and rely heavily on the API specification of Eclipse
		//   If we cannot get file path here, it will be very likely for TabNine core to response empty proposal
		
		// STEP Number Try to get file path by {@link IURIEditorInput}
		IURIEditorInput uriEditorInput = editorInput.getAdapter(IURIEditorInput.class);
		if (uriEditorInput != null) {
			File file = URIUtil.toFile(uriEditorInput.getURI());
			filePath = (file == null) ? null : file.getAbsolutePath();
			if (TabNineTextUtils.isNotBlank(filePath)) {
				return filePath;
				
			}
			
		}
		
		// STEP Number Try to get file path by {@link IFile}
		IFile file = editorInput.getAdapter(IFile.class);
		if (file != null) {
			IPath rawLocation = file.getRawLocation();
			filePath = (rawLocation == null) ? null : rawLocation.toOSString();
			if (TabNineTextUtils.isNotBlank(filePath)) {
				return filePath;
				
			}
			
		}
		
		// STEP Number If no path got from all ways, return null
		return null;
		
	}
	
	/**
	 * Load the {@link Image} object of TabNine logo
	 * @return {@link #tabNineLogoImage}
	 * @author ZhouYi
	 * @date 2019-12-20 14:49:51
	 * @description description
	 * @note note
	 */
	public static Image loadTabNineLogoImage() {
		// STEP Number If the static image object dose not exist, load it
		if (tabNineLogoImage == null) {
			try {
				// SUBSTEP Number Get root folder
				File pluginRootFolder = TabNineIOUtils.getPluginRootFolder();
				
				// SUBSTEP Number Get resource
				File imageFile = null;
				for (int i = 0, length = TABNINE_LOGO_IMAGE_PATHS.size(); i < length; i++) {
					imageFile = new File(pluginRootFolder, TABNINE_LOGO_IMAGE_PATHS.get(i));
					if (imageFile.isFile()) {
						break;
						
					}
					
				}
				if (imageFile == null) {
					throw new IOException("Fail to load TabNine logo image: No image files exists");
				}
				
				// SUBSTEP Number Create image
				tabNineLogoImage = new Image(null, new FileInputStream(imageFile));
					
			} catch (IOException e) {
				TabNineLoggingUtils.error("Fail to load TabNine logo image: Unknown IOException happended", e);
				
			} catch (URISyntaxException e) {
				TabNineLoggingUtils.error("Fail to load TabNine logo image: Unknown URISyntaxException happended", e);
				
			}
			
		}
		
		// STEP Number Return the static image object
		return tabNineLogoImage;
		
	}
	
	// ===== ===== ===== ===== [Constructors] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Factory Methods] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Getters & Setters] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [SplitLineTitle] ===== ===== ===== ===== //
	
}
