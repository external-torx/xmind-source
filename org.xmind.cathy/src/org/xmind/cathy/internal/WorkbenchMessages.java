/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.cathy.internal;

import org.eclipse.osgi.util.NLS;

public class WorkbenchMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.cathy.internal.messages"; //$NON-NLS-1$

    public static String AppWindowTitle;
    public static String ProWindowTitle;

    public static String File_menu_text;
    public static String Edit_menu_text;
    public static String Help_menu_text;
    public static String SaveAction_text;

    public static String GeneralPrefPage_title;
    public static String RecentFiles_label;
    public static String RestoreLastSession_label;
    public static String CheckUpdates_label;
    public static String AutoBackupIndicator_AutoSaveDisabled_description;

    public static String AutoBackupIndicator_AutoSaveDisabled_label;

    public static String AutoBackupIndicator_AutoSaveEnabled_label;

    public static String AutoBackupIndicator_DisableAutoSaveAction_text;

    public static String AutoBackupIndicator_EnableAutoSaveAction_text;

    public static String AutoBackupIndicator_OpenPreferenceAction_text;

    public static String AutoSave_label;
    public static String AutoSave_Minutes;
    public static String AutoSave_label2;
    public static String Startup_title;
    public static String StartupAction_label;
    public static String StartupAction_OpenDialog;
    public static String StartupAction_BlankMap;
    public static String StartupAction_HomeMap;
    public static String StartupAction_LastSession;
    public static String HomeMap_label;
    public static String HomeMap_NotFound_error;

    public static String appWindow_ListSelectionDialog_Text;

    public static String appWindow_ListSelectionDialog_Title;

    public static String KeyAssist_text;

    public static String Help_text;
    public static String Help_toolTip;
    public static String Update_text;
    public static String Update_toolTip;

    public static String CheckOpenFilesJob_CheckFiles_name;
    public static String CheckOpenFilesJob_OpenFiles_name;
    public static String CheckOpenFilesJob_FailsToOpen_message;
    public static String CheckOpenFilesJob_ShowPresentation_name;

    public static String CheckRecoverFilesJob_jobName;
    public static String CheckRecoverFilesJob_LoadFiles_name;
    public static String CheckRecoverFilesJob_FilterFiles_name;
    public static String CheckRecoverFilesJob_RecoverFiles_name;

    public static String CheckUpdatesJob_jobName;
    public static String CheckUpdatesJob_Fail_message;
    public static String CheckUpdatesJob_NoUpdate_message;
    public static String CheckUpdatesJob_NewUpdate_message;
    public static String CheckUpdatesJob_NewUpdate_info_message;
    public static String CheckUpdatesJob_NewUpdate_moreDownloads_text;

    public static String StartupJob_jobName;
    public static String StartupJob_OpenBootstrapEditor_name;
    public static String StartupJob_OpenBlankMap;
    public static String StartupJob_OpenHomeMap;
    public static String StartupJob_OpenLastSession;

    public static String PromptSaveEditorOnClosing_message;

    public static String About_LicensedTo;
    public static String About_ProTitle;
    public static String About_PlusTitle;
    public static String About_ProSubscriptionTitle;
    public static String About_LicenseTypePattern;
    public static String About_LicenseType_Unactivated;

    static {
        NLS.initializeMessages(BUNDLE_NAME, WorkbenchMessages.class);
    }

}