package com.tabnine.eclipse;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import com.tabnine.eclipse.application.TabNineApplication;
import com.tabnine.eclipse.application.impl.TabNineApplicationBasicImpl;
import com.tabnine.eclipse.data.AutocompleteArgs;
import com.tabnine.eclipse.data.AutocompleteResponse;
import com.tabnine.eclipse.util.TabNineDocumentUtils;
import com.tabnine.eclipse.util.TabNineLoggingUtils;

/**
 * The completion proposal computer using TabNine for JavaScript text
 * @author ZhouYi
 * @date 2019-12-20 11:05:59
 * @description description
 * @note note
 */
public class TabNineJavaScriptCompletionProposalComputer implements IJavaCompletionProposalComputer {

	// ===== ===== ===== ===== [Constants] ===== ===== ===== ===== //
	
	/** The option for text range of computation : int COMPUTE_RANGE_OPTION */
	public static final int OPTION_COMPUTE_RANGE = TabNineDocumentUtils.FROM_DOC;
	
	/** The option for max results count getting from TabNine : int MAX_NUM_RESULTS */
	public static final int OPTION_MAX_NUM_RESULTS = 20;
	
	// ===== ===== ===== ===== [Static Variables] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Entry Method (For test only)] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Test Methods] ===== ===== ===== ===== //

	
	// ===== ===== ===== ===== [Instance Variables] ===== ===== ===== ===== //
	
	/** The application interface of TabNine : TabNineApplication tabNineApplication */
	private TabNineApplication tabNineApplication = new TabNineApplicationBasicImpl();
	
	// ===== ===== ===== ===== [Instance Methods] ===== ===== ===== ===== //
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#computeCompletionProposals(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 * @param context
	 * @param monitor
	 * @return
	 * @writer ZhouYi
	 * @date 2019-12-20 11:28:21
	 * @description description
	 */
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		// STEP Number Get the request from incoming context object
		AutocompleteArgs autocompleteArgs = TabNineDocumentUtils.extractAutocompleteArgs(context, OPTION_COMPUTE_RANGE, null);
		
		// STEP Number Check the result we got
		if (autocompleteArgs == null) {
			TabNineLoggingUtils.info("The autocomplete argument object is null.");
			return null;
			
		}
		
		// STEP Number Complete other settings
		autocompleteArgs.setFilename(TabNineDocumentUtils.getPathOfCurrentlyEditingFile());
		autocompleteArgs.setMaxNumResults(OPTION_MAX_NUM_RESULTS);
		
		// STEP Number Send request
		AutocompleteResponse response = tabNineApplication.autocomplete(autocompleteArgs);
		
		// STEP Number Generate proposals using the context and response, then return it
		return TabNineDocumentUtils.generateCompletionProposal(context, response, OPTION_COMPUTE_RANGE, true);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#computeContextInformation(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 * @param context
	 * @param monitor
	 * @return
	 * @writer ZhouYi
	 * @date 2019-12-20 11:28:24
	 * @description description
	 */
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#getErrorMessage()
	 * @return
	 * @writer ZhouYi
	 * @date 2019-12-20 11:28:26
	 * @description description
	 */
	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
	 * @writer ZhouYi
	 * @date 2019-12-20 11:28:30
	 * @description description
	 */
	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
	 * @writer ZhouYi
	 * @date 2019-12-20 11:28:32
	 * @description description
	 */
	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub
		
	}
	
	// ===== ===== ===== ===== [Basic API Methods] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Instance Utility Methods - Utility] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Utility Methods - Utility] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Constructors] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Static Factory Methods] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [Getters & Setters] ===== ===== ===== ===== //
	
	
	// ===== ===== ===== ===== [SplitLineTitle] ===== ===== ===== ===== //
	
}
