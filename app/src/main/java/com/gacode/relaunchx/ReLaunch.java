package com.gacode.relaunchx;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.gacode.relaunchx.FileSystem.bytesToString;


public class ReLaunch extends Activity {

	final static String TAG = "ReLaunchX";
	static public final String APP_LRU_FILE = "AppLruFile.txt";
	static public final String APP_FAV_FILE = "AppFavorites.txt";
	static public final String LRU_FILE = "LruFile.txt";
	static public final String FAV_FILE = "Favorites.txt";
	static public final String HIST_FILE = "History.txt";
	static public final String FILT_FILE = "Filters.txt";
	static public final String COLS_FILE = "Columns.txt";
	final String defReaders = ".fb2,.fb2.zip:org.coolreader%org.coolreader.CoolReader%Cool Reader"
			+ "|.epub:Intent:application/epub+zip"
			+ "|.jpg,.jpeg:Intent:image/jpeg"
			+ "|.png:Intent:image/png"
			+ "|.pdf:Intent:application/pdf"
			+ "|.djv,.djvu:Intent:image/vnd.djvu"
			+ "|.doc:Intent:application/msword"
			+ "|.chm,.pdb,.prc,.mobi,.azw:org.coolreader%org.coolreader.CoolReader%Cool Reader"
			+ "|.cbz,.cb7:Intent:application/x-cbz"
			+ "|.cbr:Intent:application/x-cbr";
	final static public String defReader = "org.coolreader%org.coolreader.CoolReader%Cool Reader";
	final static public int TYPES_ACT = 1;
	final static public int DIR_ACT = 2;
	final static int CNTXT_MENU_DELETE_F = 1;
	final static int CNTXT_MENU_DELETE_D_EMPTY = 2;
	final static int CNTXT_MENU_DELETE_D_NON_EMPTY = 3;
	final static int CNTXT_MENU_ADD = 4;
	final static int CNTXT_MENU_CANCEL = 5;
	final static int CNTXT_MENU_MARK_READING = 6;
	final static int CNTXT_MENU_MARK_FINISHED = 7;
	final static int CNTXT_MENU_MARK_FORGET = 8;
	final static int CNTXT_MENU_INTENT = 9;
	final static int CNTXT_MENU_OPENWITH = 10;
	final static int CNTXT_MENU_COPY_FILE = 11;
	final static int CNTXT_MENU_MOVE_FILE = 12;
	final static int CNTXT_MENU_PASTE = 13;
	final static int CNTXT_MENU_RENAME = 14;
	final static int CNTXT_MENU_CREATE_DIR = 15;
	final static int CNTXT_MENU_COPY_DIR = 16;
	final static int CNTXT_MENU_MOVE_DIR = 17;
	final static int CNTXT_MENU_SWITCH_TITLES = 18;
	final static int CNTXT_MENU_TAGS_RENAME = 19;
	final static int CNTXT_MENU_ADD_STARTDIR = 20;
	final static int CNTXT_MENU_SHOW_BOOKINFO = 21;
	final static int CNTXT_MENU_FILE_INFO = 22;
	final static int CNTXT_MENU_SET_STARTDIR = 23;
	final static int BROWSE_FILES = 0;
	final static int BROWSE_TITLES = 1;
	final static int BROWSE_COVERS = 2;
	String currentRoot = "/sdcard";
	Integer currentPosition = -1;
	List<FileDetails> itemsArray;
	Stack<Integer> positions = new Stack<Integer>();
	Stack<Integer> scrollPositions = new Stack<Integer>();
	BaseAdapter adapter;
	SharedPreferences prefs;
	ReLaunchApp app;
	static public boolean useHome = false;
	static boolean useHome1 = false;
	static boolean useShop = false;
	static boolean useLibrary = false;
	boolean useDirViewer = false;
	static public boolean filterMyself = true;
	static public String selfName = "com.gacode.relaunchx.Main";
	boolean addSView = true;

	// multicolumns per directory configuration
	List<String[]> columnsArray = new ArrayList<String[]>();
	Integer currentColsNum = -1;

	// Bottom info panel
	BroadcastReceiver batteryLevelReceiver = null;
	boolean batteryLevelRegistered = false;
	boolean mountReceiverRegistered = false;
	boolean powerReceiverRegistered = false;
	boolean wifiReceiverRegistered = false;
	TextView memTitle;
	TextView memLevel;
	TextView battTitle;
	TextView battLevel;
	IntentFilter batteryLevelFilter;

	String fileOpFile;
	String fileOpDir;
	int fileOp;

	enum SortMode {
		Ascending,
		Descending
	}
	SortMode sortMode = SortMode.Ascending;

    enum SortKey {
        BookTitle, //used only when Show Book Titles is on
        FileName,
        FileExtension,
        FileSize,
        FileDate,
    }
    SortKey sortKey = SortKey.FileName;

    public enum FsItemType {
        File,
        Directory
    };

    private final class FileDetails {
        public String name;
        public String displayName;
        public String extension;
        public String directoryName;
        public String fullPathName;
        public FsItemType type;
        public Date date;
        public long size;
        public String reader;
    }

	private void actionSwitchWiFi() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			// "WiFi is off"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunchx_turning_wifi_off),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(false);
		} else {
			// "WiFi is ON"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunchx_turning_wifi_on),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(true);
		}
		refreshBottomInfo();
	}

	private void actionLock() {
		PowerFunctions.actionLock(ReLaunch.this);
	}

	private void actionPowerOff() {
		PowerFunctions.actionPowerOff(ReLaunch.this);
	}

	private void saveLast() {
		int appLruMax = 30;
		try {
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
	}

	private void actionRun(String appspec) {
		Intent i = app.getIntentByLabel(appspec);
		if (i == null)
			// "Activity \"" + item + "\" not found!"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(R.string.jv_allapp_activity)
							+ " \""
							+ appspec
							+ "\" "
							+ getResources().getString(
									R.string.jv_allapp_not_found),
					Toast.LENGTH_LONG).show();
		else {
			boolean ok = true;
			try {
				i.setAction(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				// "Activity \"" + item + "\" not found!"
				Toast.makeText(
						ReLaunch.this,
						getResources().getString(R.string.jv_allapp_activity)
								+ " \""
								+ appspec
								+ "\" "
								+ getResources().getString(
										R.string.jv_allapp_not_found),
						Toast.LENGTH_LONG).show();
				ok = false;
			}
			if (ok) {
				app.addToList("app_last", appspec, "X", false);
				saveLast();
			}
		}
	}

	private void setEinkController() {
		if (prefs != null) {
			Integer einkUpdateMode = 1;
			try {
				einkUpdateMode = Integer.parseInt(prefs.getString(
						"einkUpdateMode", "1"));
			} catch (Exception e) {
				einkUpdateMode = 1;
			}
			if (einkUpdateMode < -1 || einkUpdateMode > 2)
				einkUpdateMode = 1;
			if (einkUpdateMode >= 0) {
				EinkScreen.UpdateMode = einkUpdateMode;

				Integer einkUpdateInterval = 10;
				try {
					einkUpdateInterval = Integer.parseInt(prefs.getString(
							"einkUpdateInterval", "10"));
				} catch (Exception e) {
					einkUpdateInterval = 10;
				}
				if (einkUpdateInterval < 0 || einkUpdateInterval > 100)
					einkUpdateInterval = 10;
				EinkScreen.UpdateModeInterval = einkUpdateInterval;

				EinkScreen.PrepareController(null, false);
			}
		}
	}

	private void checkDevice(String dev, String man, String model,
			String product) {
		if (DeviceInfo.isCompatibleDevice(app))
			return;

		if (!prefs.getBoolean("allowDevice", false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			WebView wv = new WebView(this);
			wv.loadDataWithBaseURL(null,
					getResources().getString(R.string.model_warning),
					"text/html", "utf-8", null);
			// "Wrong model !"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_wrong_model));
			builder.setView(wv);
			// "YES"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunchx_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putBoolean("allowDevice", true);
							editor.commit();
							dialog.dismiss();
						}
					});
			// "NO"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
					});

			builder.show();
		}
	}

	static class ViewHolder {
		TextView tv;
		TextView tv2;
        TextView tvSizeDate;
		ImageView iv;
		TextView tvicon;
		ImageView is;
		LinearLayout tvHolder;
	}

	private Bitmap scaleDrawableById(int id, int size) {
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), id), size, size,
				true);
	}

	private Bitmap scaleDrawable(Drawable d, int size) {
		return Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),
				size, size, true);
	}

	class FilesViewAdapter extends BaseAdapter {
        private Context ctx = null;
        FilesViewAdapter(Context context) {
            this.ctx = context;
        }

		@Override
		public int getCount() {
            return itemsArray.size();
		}

        @Override
        public Object getItem(int position) {
            return itemsArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
//			if ((prefs.getBoolean("showBookTiles", false)) || (v == null)) {
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.flist_layout, null);
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.fl_text);
                holder.tv2 = (TextView) v.findViewById(R.id.fl_text2);
                holder.tvSizeDate = (TextView) v.findViewById(R.id.fl_SizeDate);
                holder.tvicon = (TextView) v.findViewById(R.id.textViewIcon);
				holder.iv = (ImageView) v.findViewById(R.id.fl_icon);
				holder.is = (ImageView) v.findViewById(R.id.fl_separator);
				holder.tvHolder = (LinearLayout) v.findViewById(R.id.fl_holder);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();
			if (prefs.getBoolean("doNotHyph", false)) {
				holder.tv.setLines(1);
				holder.tv.setHorizontallyScrolling(true);
				holder.tv.setEllipsize(TruncateAt.END);
				holder.tv2.setLines(1);
				holder.tv2.setHorizontallyScrolling(true);
				holder.tv2.setEllipsize(TruncateAt.END);
			}
			// known extensions
			List<HashMap<String, String>> rc;
			ArrayList<String> exts = new ArrayList<String>();

			if (prefs.getBoolean("hideKnownExts", false)) {
				rc = app.getReaders();
				Set<String> tkeys = new HashSet<String>();
				for (int i = 0; i < rc.size(); i++) {
					Object[] keys = rc.get(i).keySet().toArray();
					for (int j = 0; j < keys.length; j++) {
						tkeys.add(keys[j].toString());
					}
				}
				exts = new ArrayList<String>(tkeys);
				final class ExtsComparator implements
						java.util.Comparator<String> {
					public int compare(String a, String b) {
						if (a == null && b == null)
							return 0;
						if (a == null && b != null)
							return 1;
						if (a != null && b == null)
							return -1;
						if (a.length() < b.length())
							return 1;
						if (a.length() > b.length())
							return -1;
						return a.compareTo(b);
					}
				}
				Collections.sort(exts, new ExtsComparator());
			}

			FileDetails item = itemsArray.get(position);
			if (item != null) {
				TextView tv = holder.tv;
				TextView tv2 = holder.tv2;
                TextView tvSizeDate = holder.tvSizeDate;
				LinearLayout tvHolder = holder.tvHolder;
				ImageView iv = holder.iv;
				ImageView is = holder.is;
				if (!prefs.getBoolean("rowSeparator", false))
					is.setVisibility(View.GONE);

				String sname = item.displayName;
				// clean extension, if needed
				if (prefs.getBoolean("showIcon", false) == false) {
					if (item.extension != null && !item.extension.equals(""))
						sname = sname.substring(0, sname.length() - (item.extension.length() + 1));
				} else if (prefs.getBoolean("hideKnownExts", false) && !prefs.getBoolean("showBookTitles", false)) {
					for (int i = 0; i < exts.size(); i++) {
						if (sname.endsWith(exts.get(i))) {
							sname = sname.substring(0, sname.length() - exts.get(i).length());
						}
					}
				}

				String fname = item.fullPathName;
				boolean setBold = false;
				boolean useFaces = prefs.getBoolean("showNew", true);

				SizeManipulation.AdjustWithPreferencesFileListLine1(app, prefs, tv);
				SizeManipulation.AdjustWithPreferencesFileListLine2(app, prefs, tv2);
				SizeManipulation.AdjustWithPreferencesFileListLine2(app, prefs, tvSizeDate);

                if (item.type == FsItemType.Directory) {
					tv2.setVisibility(View.GONE);
					tv2.getLayoutParams().height = 0;
					if (useFaces) {
						tvHolder.setBackgroundColor(getResources().getColor(
								R.color.dir_bg));
						tv.setTextColor(getResources().getColor(R.color.dir_fg));
					}
					if (SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, R.drawable.dir_ok)) {
						holder.tvicon.setVisibility(View.GONE);
					}else {
						holder.iv.setVisibility(View.GONE);
						holder.tvicon.setText(">");
					}
				} else {
					if (useFaces) {
						if (app.history.containsKey(fname)) {
							if (app.history.get(fname) == app.READING) {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_reading_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_reading_fg));
								tv2.setTextColor(getResources().getColor(
										R.color.file_reading_fg));
							} else if (app.history.get(fname) == app.FINISHED) {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_finished_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_finished_fg));
								tv2.setTextColor(getResources().getColor(
										R.color.file_finished_fg));
							} else {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_unknown_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_unknown_fg));
								tv2.setTextColor(getResources().getColor(
										R.color.file_unknown_fg));
							}
						} else {
							tvHolder.setBackgroundColor(getResources()
									.getColor(R.color.file_new_bg));
							tv.setTextColor(getResources().getColor(
									R.color.file_new_fg));
							tv2.setTextColor(getResources().getColor(
									R.color.file_new_fg));
							if (getResources().getBoolean(
									R.bool.show_new_as_bold))
								setBold = true;
						}
					}

					// setup icon
					if (SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv))
					{
						holder.tvicon.setVisibility(View.GONE);
						Drawable d = app.specialIcon(item.fullPathName, false);
						if (d != null)
							SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, d);
						else {
							String rdrName = item.reader;
							if (rdrName.equals("Nope")) {
								File f = new File(item.fullPathName);
								if (f.length() > app.viewerMax*1024)
									SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, R.drawable.file_notok);
								else
									SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, R.drawable.file_ok);
							} else if (rdrName.startsWith("Intent:"))
								SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, R.drawable.icon);
							else {
								if (app.getIcons().containsKey(rdrName))
									SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, app.getIcons().get(rdrName));
								else
									SizeManipulation.AassignWithPreferencesIcon(app, prefs, iv, R.drawable.file_ok);
							}
						}
					} else {
						iv.setVisibility(View.GONE);
						holder.tvicon.setText(item.extension.toUpperCase());
					}
				}
//TODO check the file bitmap and extension
				String sname1 = sname;
				String sname2 = "";
				int newLinePos = sname.indexOf('\n');
				if ((newLinePos != -1) && (newLinePos != 0)) {
					sname1 = sname.substring(0, newLinePos).trim();
					sname2 = sname.substring(newLinePos, sname.length()).trim();
				}
				if (useFaces) {
					SpannableString s1 = new SpannableString(sname1);
					s1.setSpan(new StyleSpan(setBold ? Typeface.BOLD
							: Typeface.NORMAL), 0, sname1.length(), 0);
					tv.setText(s1);
					if (!sname2.equalsIgnoreCase("")) {
						SpannableString s2 = new SpannableString(sname2);
						s2.setSpan(new StyleSpan(setBold ? Typeface.BOLD
							: Typeface.NORMAL), 0, sname2.length(), 0);
						tv2.setText(s2);
					}
				} else {
					tvHolder.setBackgroundColor(getResources().getColor(
							R.color.normal_bg));
					tv.setTextColor(getResources().getColor(R.color.normal_fg));
					tv.setText(sname1);
					tv2.setTextColor(getResources().getColor(R.color.normal_fg));
					tv2.setText(sname2);
				}
				if (sname2.equalsIgnoreCase("")) {
					tv2.setVisibility(View.GONE);
				} else {
					tv2.setVisibility(View.VISIBLE);
				}

                boolean displayFileSizeAndDate = prefs.getBoolean("showFileDetails", false);

                String fileSizeDate = "";
                if (displayFileSizeAndDate && item.type == FsItemType.File) {
                    fileSizeDate += getResources().getString(R.string.jv_relaunchx_fileinfo_size) + " " + bytesToString(item.size);
                    final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy.MM.dd");
                    fileSizeDate += " " + getResources().getString(R.string.jv_relaunchx_fileinfo_date)
                            + " " + String.valueOf(fileDateFormat.format(item.date));
                }
                tvSizeDate.setText(fileSizeDate);

                if (fileSizeDate.equalsIgnoreCase("")) {
                    tvSizeDate.setVisibility(View.GONE);
                } else {
                    tvSizeDate.setVisibility(View.VISIBLE);
                }
			}
			// fixes on rows height in grid
			if (currentColsNum != 1) {
				GridView pgv = (GridView) parent;
				Integer gcols = currentColsNum;
				Integer between_columns = 0; // configure ???
				Integer after_row_space = 0; // configure ???
				Integer colw = (pgv.getWidth() - (gcols - 1) * between_columns)
						/ gcols;
				Integer recalc_num = position;
				Integer recalc_height = 0;
				while (recalc_num % gcols != 0) {
					recalc_num = recalc_num - 1;
					View temp_v = getView(recalc_num, null, parent);
					temp_v.measure(MeasureSpec.EXACTLY | colw,
							MeasureSpec.UNSPECIFIED);
					Integer p_height = temp_v.getMeasuredHeight();
					if (p_height > recalc_height)
						recalc_height = p_height;
				}
				if (recalc_height > 0) {
					v.setMinimumHeight(recalc_height + after_row_space);
				}
			}
			return v;
		}
	}

	private Integer getAutoColsNum() {
		ArrayList<Integer> textLength = new ArrayList<Integer>();
		if (itemsArray.size() > 0) {
			for (Integer i = 0; i < itemsArray.size(); i++) {
				textLength.add(itemsArray.get(i).displayName.length());
			}
		}

		return SizeManipulation.AutoColumnsNumber(app, prefs, textLength);
	}

	private void redrawList() {
		setEinkController();
		GridView gv = (GridView) findViewById(useDirViewer ? R.id.results_list : R.id.gl_list);
		if (prefs.getBoolean("filterResults", false)) {
			List<FileDetails> newItemsArray = new ArrayList<FileDetails>();

			for (FileDetails item : itemsArray) {
				if (item.type == FsItemType.Directory
						|| app.filterFile(item.directoryName, item.name))
					newItemsArray.add(item);
			}
			itemsArray = newItemsArray;
		}
		adapter.notifyDataSetChanged();
		int curPos = prefs.getInt("posInFolder", 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("posInFolder", 0);
		editor.commit();
		gv.setSelection(curPos);
//		gv.invalidate();
	}

	private static List<HashMap<String, String>> parseReadersString(
			String readerList) {
		List<HashMap<String, String>> rc = new ArrayList<HashMap<String, String>>();
		String[] rdrs = readerList.split("\\|");
		for (int i = 0; i < rdrs.length; i++) {
			String[] re = rdrs[i].split(":");
			switch (re.length) {
			case 2:
				String rName = re[1];
				String[] exts = re[0].split(",");
				for (int j = 0; j < exts.length; j++) {
					String ext = exts[j];
					HashMap<String, String> r = new HashMap<String, String>();
					r.put(ext, rName);
					rc.add(r);
				}
				break;
			case 3:
				if (re[1].equals("Intent")) {
					String iType = re[2];
					String[] exts1 = re[0].split(",");
					for (int j = 0; j < exts1.length; j++) {
						String ext = exts1[j];
						HashMap<String, String> r = new HashMap<String, String>();
						r.put(ext, "Intent:" + iType);
						rc.add(r);
					}
				}
				break;
			}
		}
		return rc;
	}

	public static String createReadersString(List<HashMap<String, String>> rdrs) {
		String rc = new String();

		for (HashMap<String, String> r : rdrs) {
			for (String key : r.keySet()) {
				if (!rc.equals(""))
					rc += "|";
				rc += key + ":" + r.get(key);
			}
		}
		return rc;
	}

	private void pushCurrentPos(AdapterView<?> parent, boolean push_to_stack) {
		Integer p1 = parent.getFirstVisiblePosition();
		if (push_to_stack)
			positions.push(p1);
		currentPosition = p1;
	}

	private void setUpButtonIcons() {
        TextView filterOutputListIcon = (TextView) findViewById(R.id.filter_results_icon);
        TextView showHiddenItemsIcon = (TextView) findViewById(R.id.show_hidden_icon);
        if (prefs.getBoolean("filterResults", false)) {
            filterOutputListIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filter_on, 0,0,0);
        } else {
            filterOutputListIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filter_off, 0,0,0);
        }
        if (prefs.getBoolean("showHidden", false)) {
            showHiddenItemsIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.show_hidden_on, 0,0,0);
        } else {
            showHiddenItemsIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.show_hidden_off, 0,0,0);
        }
    }

	private void setUpButton(final Button up, final String upDir, String currDir) {
		if (up != null) {
            setUpButtonIcons();

			// gesture listener
			class UpSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (prefs.getBoolean("notLeaveStartDir", false)) {
                        String[] homes = prefs.getString("startDir",
                                "/sdcard,/media/My Files").split("\\,");
                        for (int i = 0; i < homes.length; i++) {
                            if (homes[i].length() == currentRoot.length() && currentRoot.equals(homes[i])) {
                                return true;
                            }
                        }
                    }
					if (!upDir.equals("")) {
						Integer p = -1;
						if (!positions.empty())
							p = positions.pop();
						drawDirectory(upDir, p);
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
                    final CharSequence[] items = {
                            app.getResources().getString(R.string.pref_i_filterResults_title),
                            app.getResources().getString(R.string.pref_i_showHidden_title)
                    };
                    final boolean[] checkStates = new boolean[2];
                    checkStates[0] = prefs.getBoolean("filterResults", false);
                    checkStates[1] = prefs.getBoolean("showHidden", false);

                    AlertDialog dialog = new AlertDialog.Builder(ReLaunch.this)
                            .setTitle(app.getResources().getString(R.string.pref_s_file_title))
                            .setMultiChoiceItems(items, checkStates, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    checkStates[indexSelected] = isChecked;
                                }
                            }).setPositiveButton(app.getResources().getString(R.string.jv_relaunchx_viewOptions_adjustFilters_title),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("filterResults", checkStates[0]);
                                    editor.putBoolean("showHidden", checkStates[1]);
                                    editor.commit();
                                    Intent intent = new Intent(ReLaunch.this, FiltersActivity.class);
                                    startActivityForResult(intent, -1);

                                }
                            }).setNegativeButton(app.getResources().getString(R.string.jv_relaunchx_viewOptions_OK),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("filterResults", checkStates[0]);
                                    editor.putBoolean("showHidden", checkStates[1]);
                                    editor.commit();
                                    drawDirectory(currentRoot, currentPosition);
                                }
                            }).create();
                    dialog.show();
				}
			}
			;
			UpSimpleOnGestureListener up_gl = new UpSimpleOnGestureListener();
			final GestureDetector up_gd = new GestureDetector(up_gl);
			up.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					up_gd.onTouchEvent(event);
					return false;
				}
			});
		}
	}

	private void refreshBottomInfo() {
		boolean longLabelsMode = prefs.getBoolean("toolbarTextMode", true);


		// Date
		String d;
		Calendar c = Calendar.getInstance();
		if (prefs.getBoolean("dateUS", false))
			d = String.format("%02d:%02d%s %02d/%02d/%02d",
					c.get(Calendar.HOUR), c.get(Calendar.MINUTE),
					((c.get(Calendar.AM_PM) == 0) ? "AM" : "PM"),
					c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
					(c.get(Calendar.YEAR) - 2000));
		else
			d = String.format("%02d:%02d %02d/%02d/%02d",
					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
					c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
					(c.get(Calendar.YEAR) - 2000));
		// Memory
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);

		if (longLabelsMode) {
			if (memTitle != null) {
				memTitle.setText(d);
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, memTitle);
			}

			if (memLevel != null) {
				// "M free"
				memLevel.setText(mi.availMem / 1048576L
						+ getResources().getString(R.string.jv_relaunchx_m_free));
				memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.drawable.ram), null);
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, memLevel);
			}
		} else {
			if (memTitle != null) {
				memTitle.setText("");
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, memTitle);
			}

			if (memLevel != null) {
				memLevel.setText(mi.availMem / 1048576L + "M");
				memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.drawable.ram), null);
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, memLevel);
			}
		}

		// Wifi status
		WifiManager wfm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (battTitle != null) {
			if (wfm.isWifiEnabled()) {
				if (longLabelsMode) {
					String nowConnected = wfm.getConnectionInfo().getSSID();
					if (nowConnected != null && !nowConnected.equals("")) {
						battTitle.setText(nowConnected);
					} else {
						battTitle.setText(getResources().getString(
								R.string.jv_relaunchx_wifi_is_on));
					}
				} else {
					battTitle.setText(getResources().getString(
							R.string.jv_relaunchx_wifi));
				}
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, battTitle);
				battTitle.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.wifi_on), null,
						null, null);
			} else {
				// "WiFi is off"
				if (longLabelsMode) {
					battTitle.setText(getResources().getString(
							R.string.jv_relaunchx_wifi_is_off));
				} else {
					battTitle.setText("");
				}
				SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, battTitle);
				battTitle.setCompoundDrawablesWithIntrinsicBounds(
						null, null,
						null, null);
			}
		}

		// Battery
		if (batteryLevelReceiver == null) {
			batteryLevelReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					try {
						boolean longLabelsMode = prefs.getBoolean("toolbarTextMode", true);

						context.unregisterReceiver(this);
						batteryLevelRegistered = false;
						int rawlevel = intent.getIntExtra(
								BatteryManager.EXTRA_LEVEL, -1);
						int scale = intent.getIntExtra(
								BatteryManager.EXTRA_SCALE, -1);
						int plugged = intent.getIntExtra(
								BatteryManager.EXTRA_PLUGGED, -1);
						int level = -1;
						if (rawlevel >= 0 && scale > 0) {
							level = (rawlevel * 100) / scale;
						}
						if (battLevel != null) {
							if (longLabelsMode) {
								String add_text = "";
								if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
									add_text = " AC";
								} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
									add_text = " USB";
								}
								battLevel.setText(level + "%" + add_text);
							} else {
								battLevel.setText(level + "%");
							}
							SizeManipulation.AdjustWithPreferencesToolbarText(app, prefs, battLevel);

							if (level < 25)
								battLevel
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources().getDrawable(
														R.drawable.bat1), null,
												null, null);
							else if (level < 50)
								battLevel
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources().getDrawable(
														R.drawable.bat2), null,
												null, null);
							else if (level < 75)
								battLevel
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources().getDrawable(
														R.drawable.bat3), null,
												null, null);
							else
								battLevel
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources().getDrawable(
														R.drawable.bat4), null,
												null, null);
						}
					} catch (IllegalArgumentException e) {
						Log.v("ReLaunch", "Battery intent illegal arguments");
					}

				}
			};
		}
		if (!batteryLevelRegistered) {
			registerReceiver(batteryLevelReceiver, batteryLevelFilter);
			batteryLevelRegistered = true;
		}
	}

	private void drawDirectory(String root, Integer startPosition) {

        File dir = new File(root);
		File[] allEntries = dir.listFiles();
		List<File> files = new ArrayList<File>();
		List<String> dirs = new ArrayList<String>();

		setEinkController();

		currentRoot = root;
		currentPosition = (startPosition == -1) ? 0 : startPosition;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastdir", currentRoot);
		editor.commit();

		final Button tv = (Button) findViewById(useDirViewer ? R.id.results_title
				: R.id.title_txt);
		final String dirAbsPath = dir.getAbsolutePath();
		if (prefs.getBoolean("showFullDirPath", true))
			tv.setText(dirAbsPath + " ("
				+ ((allEntries == null) ? 0 : allEntries.length) + ")");
		else
			tv.setText(dir.getName());
		class TvSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				final String[] columns = getResources().getStringArray(
						R.array.output_columns_names);
				final CharSequence[] columnsmode = new CharSequence[columns.length + 1];
				columnsmode[0] = getResources().getString(
						R.string.jv_relaunchx_default);
				for (int i = 0; i < columns.length; i++) {
					columnsmode[i + 1] = columns[i];
				}
				Integer checked = -1;
				if (app.columns.containsKey(currentRoot)) {
					if (app.columns.get(currentRoot) == -1) {
						checked = 1;
					} else {
						checked = app.columns.get(currentRoot) + 1;
					}
				} else {
					checked = 0;
				}
				// get checked
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ReLaunch.this);
				// "Select application"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_select_columns));
				builder.setSingleChoiceItems(columnsmode, checked,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i) {
								if (i == 0) {
									app.columns.remove(currentRoot);
								} else {
									if (i == 1) {
										app.columns.put(currentRoot, -1);
									} else {
										app.columns.put(currentRoot, i - 1);
									}
								}
								app.saveList("columns");
								drawDirectory(currentRoot, currentPosition);
								dialog.dismiss();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				menuSort();
			}
		}
		;
		TvSimpleOnGestureListener tv_gl = new TvSimpleOnGestureListener();
		final GestureDetector tv_gd = new GestureDetector(tv_gl);
		tv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				tv_gd.onTouchEvent(event);
				return false;
			}
		});

		final Button up = (Button) findViewById(R.id.goup_btn);
		final ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
		if (adv != null) {
			class advSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (prefs.getString("advancedButtonST", "RELAUNCH").equals(
							"RELAUNCH")) {
						Intent i = new Intent(ReLaunch.this, Advanced.class);
						startActivity(i);
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("LOCK")) {
						actionLock();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("POWEROFF")) {
						actionPowerOff();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("SWITCHWIFI")) {
						actionSwitchWiFi();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("RUN")) {
						actionRun(prefs.getString("advancedButtonSTapp", "%%"));
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					if (prefs.getString("advancedButtonDT", "NOTHING").equals(
							"RELAUNCH")) {
						Intent i = new Intent(ReLaunch.this, Advanced.class);
						startActivity(i);
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("LOCK")) {
						actionLock();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("POWEROFF")) {
						actionPowerOff();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("SWITCHWIFI")) {
						actionSwitchWiFi();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("RUN")) {
						actionRun(prefs.getString("advancedButtonDTapp", "%%"));
					}
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					if (adv.hasWindowFocus()) {
						if (prefs.getString("advancedButtonLT", "NOTHING")
								.equals("RELAUNCH")) {
							Intent i = new Intent(ReLaunch.this, Advanced.class);
							startActivity(i);
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("RUN")) {
							actionRun(prefs.getString("advancedButtonLTapp",
									"%%"));
						}
					}
				}
			}
			;
			advSimpleOnGestureListener adv_gl = new advSimpleOnGestureListener();
			final GestureDetector adv_gd = new GestureDetector(adv_gl);
			adv.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					adv_gd.onTouchEvent(event);
					return false;
				}
			});
		}

		itemsArray = new ArrayList<FileDetails>();
		if (dir.getParent() != null)
			dirs.add("..");
		if (allEntries != null) {
			for (File entry : allEntries) {
				if (entry.isDirectory())
					dirs.add(entry.getName());
				else if (!prefs.getBoolean("filterResults", false)
						|| app.filterFile(dir.getAbsolutePath(), entry.getName()))
					files.add(entry);
			}
		}

		Collections.sort(dirs);
		String upDir = "";
		for (String f : dirs) {
			if ((f.charAt(0) == '.') && (f.charAt(1) != '.') && (!prefs.getBoolean("showHidden", false)))
				continue;
			FileDetails item = new FileDetails();
			item.name = f;
			item.displayName = f;
			item.directoryName = dir.getAbsolutePath();
			if (f.equals("..")) {
				upDir = dir.getParent();
				continue;
			} else
				item.fullPathName = dir.getAbsolutePath() + File.separator + f;
			item.type = FsItemType.Directory;
			item.reader = "Nope";
			itemsArray.add(item);
		}
		List<FileDetails> fileItemsArray = new ArrayList<FileDetails>();
		for (File f : files) {
			String fname = f.getName();
			if ((fname.startsWith(".")) && (!prefs.getBoolean("showHidden", false)))
				continue;
			FileDetails item = new FileDetails();
            String[] fparts = fname.split("[.]");
            item.extension = fparts.length > 1 ? fparts[fparts.length -1] : "";
            if (prefs.getBoolean("showBookTitles", false))
				item.displayName = getEbookName(dir.getAbsolutePath(), fname);
			else
				item.displayName = fname;
			item.name = fname;
			item.directoryName = dir.getAbsolutePath();
			item.fullPathName = dir.getAbsolutePath() + File.separator + fname;
			item.type = FsItemType.File;
            item.date = new Date(f.lastModified());
            item.size = f.length();
			item.reader = app.readerName(fname.toLowerCase());
            fileItemsArray.add(item);
		}

		setSortMode(prefs.getInt("sortKey", 0), prefs.getInt("sortOrder", 0));
		fileItemsArray = sortFiles(fileItemsArray, sortKey, sortMode);
		itemsArray.addAll(fileItemsArray);
		setUpButton(up, upDir, currentRoot);
		final GridView gv = (GridView) findViewById(useDirViewer ? R.id.results_list
				: R.id.gl_list);
		adapter = new FilesViewAdapter(this);
		gv.setAdapter(adapter);

		gv.setHorizontalSpacing(0);
		Integer colsNum = -1;
		if (getDirectoryColumns(currentRoot) != 0) {
			colsNum = getDirectoryColumns(currentRoot);
		} else {
			colsNum = Integer.parseInt(prefs
					.getString("columnsDirsFiles", "-1"));
		}
		// override auto (not working fine in adnroid)
		if (colsNum == -1) {
			colsNum = getAutoColsNum();
		}
		currentColsNum = colsNum;
		gv.setNumColumns(colsNum);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth",
							"25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}
				LinearLayout ll = (LinearLayout) findViewById(useDirViewer ? R.id.results_fl
						: R.id.gl_layout);
				final SView sv = new SView(getBaseContext());
				LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
						scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
				sv.setLayoutParams(pars);
				ll.addView(sv);
				gv.setOnScrollListener(new AbsListView.OnScrollListener() {
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						sv.total = totalItemCount;
						sv.count = visibleItemCount;
						sv.first = firstVisibleItem;
						setEinkController();
						sv.invalidate();
					}

					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
					}
				});
				addSView = false;
			}
		} else {
			gv.setOnScrollListener(new AbsListView.OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					setEinkController();
				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
				}
			});
		}

		if (startPosition != -1)
			gv.setSelection(startPosition);

		class GlSimpleOnGestureListener extends SimpleOnGestureListener {
			Context context;

			public GlSimpleOnGestureListener(Context context) {
				super();
				this.context = context;
			}
			public int findViewByXY(MotionEvent e) {
			    int location[] = new int[2];
			    float x = e.getRawX();
				float y = e.getRawY();
				int first = gv.getFirstVisiblePosition();
				int last = gv.getLastVisiblePosition();
				int count = last -first + 1;
				for (int i = 0; i<count; i++) {
					View v = gv.getChildAt(i);
				    v.getLocationOnScreen(location);
				    int viewX = location[0];
				    int viewY = location[1];
				    if(( x > viewX && x < (viewX + v.getWidth())) &&
				            ( y > viewY && y < (viewY + v.getHeight()))){
				        return first + i;
				    }
				}
				return -1;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				int position = findViewByXY(e);
				if (position == -1)
					return true;
				FileDetails item = itemsArray.get(position);

				if (item.type == FsItemType.Directory) {
					// Goto directory
					pushCurrentPos(gv, true);
					drawDirectory(item.fullPathName, -1);
				} else if (app.specialAction(ReLaunch.this, item.fullPathName))
					pushCurrentPos(gv, false);
				else {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("posInFolder", gv.getFirstVisiblePosition());
					editor.commit();
					pushCurrentPos(gv, false);
					if (item.reader.equals("Nope"))
						app.defaultAction(ReLaunch.this, item.fullPathName);
					else {
						// Launch reader
						if (app.askIfAmbiguous) {
							List<String> rdrs = app.readerNames(item.fullPathName);
							if (rdrs.size() < 1)
								return true;
							else if (rdrs.size() == 1)
								start(app.launchReader(rdrs.get(0), item.fullPathName));
							else {
								final CharSequence[] applications = rdrs
										.toArray(new CharSequence[rdrs.size()]);
								final String rdr1 = item.fullPathName;
								AlertDialog.Builder builder = new AlertDialog.Builder(
										ReLaunch.this);
								// "Select application"
								builder.setTitle(getResources()
										.getString(
												R.string.jv_relaunchx_select_application));
								builder.setSingleChoiceItems(applications, -1,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int i) {
												start(app
														.launchReader(
																(String) applications[i],
																rdr1));
												dialog.dismiss();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
						} else
							start(app.launchReader(item.reader, item.fullPathName));
					}
				}
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				int position = findViewByXY(e);
				if (position != -1) {
					FileDetails item = itemsArray.get(position);
					String file = item.directoryName + File.separator + item.name;
					if (file.endsWith("fb2") || file.endsWith("fb2.zip") || file.endsWith("epub")) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt("posInFolder", gv.getFirstVisiblePosition());
						editor.commit();
						pushCurrentPos(gv, false);
						showBookInfo(file);
					}
				}
				return true;
			}

			
			@Override
			public void onLongPress(MotionEvent e) {
				if (!ReLaunch.this.hasWindowFocus())
					return;
				int menuType = 0;
				int position = findViewByXY(e);
				FileDetails item;
				String fn = null;
				String dr = null;
				FsItemType tp = null;
				String fullName = null;
				ArrayList<String> aList = new ArrayList<String>(10);
				if (position == -1)
					menuType = 0;
				else {
					item = itemsArray.get(position);
					fn = item.name;
					dr = item.directoryName;
					tp = item.type;
					fullName = dr + File.separator + fn;
					if (tp == FsItemType.Directory)
						menuType = 1;
					else if (fn.endsWith("fb2") || fn.endsWith("fb2.zip") || fn.endsWith("epub"))
						menuType = 2;
					else
						menuType = 3;
				}
				if (menuType == 0) {
					if (prefs.getBoolean("useFileManagerFunctions", true)) {
						aList.add(getString(R.string.jv_relaunchx_create_folder));
						if (fileOp != 0) {
							aList.add(getString(R.string.jv_relaunchx_paste));
						}
					}
				} else if (menuType == 1) {
					if ((!app.isStartDir(fullName)) && (prefs.getBoolean("showAddStartDir", false))) {
						aList.add(getString(R.string.jv_relaunchx_set_startdir));
						aList.add(getString(R.string.jv_relaunchx_add_startdir));
					}
					if (!app.contains("favorites", fullName, app.DIR_TAG))
						aList.add(getString(R.string.jv_relaunchx_add));
					if (prefs.getBoolean("useFileManagerFunctions", true)) {
						File d = new File(fullName);
						String[] allEntries = d.list();
						aList.add(getString(R.string.jv_relaunchx_create_folder));
						aList.add(getString(R.string.jv_relaunchx_rename));
						aList.add(getString(R.string.jv_relaunchx_move));
						if (fileOp != 0) {
							aList.add(getString(R.string.jv_relaunchx_paste));
						}
						if (allEntries != null && allEntries.length > 0) {
							aList.add(getString(R.string.jv_relaunchx_delete_non_emp_dir));
						} else {
							aList.add(getString(R.string.jv_relaunchx_delete_emp_dir));
						}
					}
					aList.add(getString(R.string.jv_relaunchx_fileinfo));
				} else if (menuType == 2) {
					aList.add(getString(R.string.jv_relaunchx_bookinfo));
					if (!app.contains("favorites", dr, fn)) {
						aList.add(getString(R.string.jv_relaunchx_add));
					}
					if (app.history.containsKey(fullName)) {
						if (app.history.get(fullName) == app.READING) {
							aList.add(getString(R.string.jv_relaunchx_mark));
						} else if (app.history.get(fullName) == app.FINISHED) {
								aList.add(getString(R.string.jv_relaunchx_unmark));
						}
						aList.add(getString(R.string.jv_relaunchx_unmarkall));
					} else {
						aList.add(getString(R.string.jv_relaunchx_mark));
					}
					if (prefs.getBoolean("openWith", true))
						aList.add(getString(R.string.jv_relaunchx_openwith));
					if (prefs.getBoolean("createIntent", false))
						aList.add(getString(R.string.jv_relaunchx_createintent));
					if (prefs.getBoolean("useFileManagerFunctions", true)) {
						if (!prefs.getBoolean("showBookTitles", false))
							aList.add(getString(R.string.jv_relaunchx_tags_rename));
						aList.add(getString(R.string.jv_relaunchx_create_folder));
						if (!prefs.getBoolean("showBookTitles", false))
							aList.add(getString(R.string.jv_relaunchx_rename));
						aList.add(getString(R.string.jv_relaunchx_copy));
						aList.add(getString(R.string.jv_relaunchx_move));
						if (fileOp != 0)
							aList.add(getString(R.string.jv_relaunchx_paste));
						aList.add(getString(R.string.jv_relaunchx_delete));
					}
					aList.add(getString(R.string.jv_relaunchx_fileinfo));
				} else if (menuType == 3) {
					if (!app.contains("favorites", dr, fn)) {
						aList.add(getString(R.string.jv_relaunchx_add));
					}
					if (app.history.containsKey(fullName)) {
						if (app.history.get(fullName) == app.READING) {
							aList.add(getString(R.string.jv_relaunchx_mark));
						} else if (app.history.get(fullName) == app.FINISHED) {
								aList.add(getString(R.string.jv_relaunchx_unmark));
						}
						aList.add(getString(R.string.jv_relaunchx_unmarkall));
					} else {
						aList.add(getString(R.string.jv_relaunchx_mark));
					}
					if (prefs.getBoolean("openWith", true))
						aList.add(getString(R.string.jv_relaunchx_openwith));
					if (prefs.getBoolean("createIntent", false))
						aList.add(getString(R.string.jv_relaunchx_createintent));
					if (prefs.getBoolean("useFileManagerFunctions", true)) {
						aList.add(getString(R.string.jv_relaunchx_create_folder));
						if (!prefs.getBoolean("showBookTitles", false))
							aList.add(getString(R.string.jv_relaunchx_rename));
						aList.add(getString(R.string.jv_relaunchx_copy));
						aList.add(getString(R.string.jv_relaunchx_move));
						if (fileOp != 0)
							aList.add(getString(R.string.jv_relaunchx_paste));
						aList.add(getString(R.string.jv_relaunchx_delete));
					}
					aList.add(getString(R.string.jv_relaunchx_fileinfo));
				}
				aList.add(getString(R.string.jv_relaunchx_cancel));
				final int pos = position;
				final String[] list = aList.toArray(new String[aList.size()]);
				
				ListAdapter cmAdapter = new ArrayAdapter<String>(
		                getApplicationContext(), R.layout.cmenu_list_item, list);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setAdapter(cmAdapter, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
						String s = list[item];
						if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_cancel)))
							onContextMenuSelected(CNTXT_MENU_CANCEL, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_delete)))
							onContextMenuSelected(CNTXT_MENU_DELETE_F, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_delete_emp_dir)))
							onContextMenuSelected(CNTXT_MENU_DELETE_D_EMPTY, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_delete_non_emp_dir)))
							onContextMenuSelected(CNTXT_MENU_DELETE_D_NON_EMPTY, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_add)))
							onContextMenuSelected(CNTXT_MENU_ADD, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_mark)))
							onContextMenuSelected(CNTXT_MENU_MARK_FINISHED, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_unmark)))
							onContextMenuSelected(CNTXT_MENU_MARK_READING, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_unmarkall)))
							onContextMenuSelected(CNTXT_MENU_MARK_FORGET, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_createintent)))
							onContextMenuSelected(CNTXT_MENU_INTENT, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_openwith)))
							onContextMenuSelected(CNTXT_MENU_OPENWITH, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_copy)))
							onContextMenuSelected(CNTXT_MENU_COPY_FILE, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_move)))
							onContextMenuSelected(CNTXT_MENU_MOVE_FILE, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_paste)))
							onContextMenuSelected(CNTXT_MENU_PASTE, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_rename)))
							onContextMenuSelected(CNTXT_MENU_RENAME, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_create_folder)))
							onContextMenuSelected(CNTXT_MENU_CREATE_DIR, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_move_dir)))
							onContextMenuSelected(CNTXT_MENU_MOVE_DIR, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_tags_rename)))
							onContextMenuSelected(CNTXT_MENU_TAGS_RENAME, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_set_startdir)))
							onContextMenuSelected(CNTXT_MENU_SET_STARTDIR, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_add_startdir)))
							onContextMenuSelected(CNTXT_MENU_ADD_STARTDIR, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_bookinfo)))
							onContextMenuSelected(CNTXT_MENU_SHOW_BOOKINFO, pos);
						else if (s.equalsIgnoreCase(getString(R.string.jv_relaunchx_fileinfo)))
							onContextMenuSelected(CNTXT_MENU_FILE_INFO, pos);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		};

		GlSimpleOnGestureListener gv_gl = new GlSimpleOnGestureListener(this);
		final GestureDetector gv_gd = new GestureDetector(gv_gl);
		gv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				gv_gd.onTouchEvent(event);
				return false;
			}
		});

		final Button upScroll = (Button) findViewById(R.id.upscroll_btn);
		if (prefs.getBoolean("disableScrollJump", true) == false) {
			upScroll.setText(app.scrollStep + "%");
		} else {
			upScroll.setText(getResources()
					.getString(R.string.jv_relaunchx_prev));
		}
		class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (DeviceInfo.EINK_NOOK) { // nook
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 100, 0);
					gv.dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 200, 0);
					gv.dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 200, 0);
					gv.dispatchTouchEvent(ev);
				} else { // other devices
					int first = gv.getFirstVisiblePosition();
					int target = 0;
					if (first == 0) {
						//clear the stack if the view was reset
						scrollPositions.clear();
					}

					if (scrollPositions.empty()) {
						int visible = gv.getLastVisiblePosition() - first + 1;
						target = first - visible;
						if (target < 0)
							target = 0;
					} else {
						target = scrollPositions.pop() + currentColsNum;
					}
					gv.setSelection(target);

					// some hack workaround against not scrolling in some cases
					gv.requestFocusFromTouch();
					gv.setSelection(target);
				}
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (prefs.getBoolean("disableScrollJump", true) == false) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					first -= (total * app.scrollStep) / 100;
					if (first < 0)
						first = 0;
					gv.setSelection(first);
					// some hack workaround against not scrolling in some cases
					if (total > 0) {
						gv.requestFocusFromTouch();
						gv.setSelection(first);
					}
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (upScroll.hasWindowFocus()) {
					if (prefs.getBoolean("disableScrollJump", true) == false) {
						int first = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						first = 0;
						gv.setSelection(first);
						// some hack workaround against not scrolling in some
						// cases
						if (total > 0) {
							gv.requestFocusFromTouch();
							gv.setSelection(first);
						}
					}
				}
			}
		}
		;
		upScrlSimpleOnGestureListener upscrl_gl = new upScrlSimpleOnGestureListener();
		final GestureDetector upscrl_gd = new GestureDetector(upscrl_gl);
		upScroll.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				upscrl_gd.onTouchEvent(event);
				return false;
			}
		});

		class RepeatedDownScroll {
			public void doIt(int first, int target, int shift) {
				final GridView gv = (GridView) findViewById(R.id.gl_list);
				final int ftarget = target + shift;
				gv.clearFocus();
				gv.setSelection(ftarget);
				if (ftarget == gv.getLastVisiblePosition()) {
					return;
				}

				final int ffirst = first;
				final int fshift = shift;
				gv.postDelayed(new Runnable() {
					public void run() {
						int nfirst = gv.getFirstVisiblePosition();
						if (nfirst == ffirst) {
							RepeatedDownScroll ds = new RepeatedDownScroll();
							ds.doIt(ffirst, ftarget, fshift + 1);
						}
					}
				}, 150);
			}
		}

		final Button downScroll = (Button) findViewById(R.id.downscroll_btn);
		if (prefs.getBoolean("disableScrollJump", true) == false) {
			downScroll.setText(app.scrollStep + "%");
		} else {
			downScroll.setText(getResources().getString(
					R.string.jv_relaunchx_next));
		}
		class dnScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (DeviceInfo.EINK_NOOK) { // nook special
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 200, 0);
					gv.dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 100, 0);
					gv.dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 100, 0);
					gv.dispatchTouchEvent(ev);
				} else { // other devices
					int first = gv.getFirstVisiblePosition();
					int target = gv.getLastVisiblePosition();
					if (first == 0) {
						scrollPositions.clear();
					} else if (prefs.getBoolean("disableScrollJump", true)) {
						//remember top element to easily go back to current view via PgUp
						if (scrollPositions.empty())
							scrollPositions.push(first);
						else if (scrollPositions.peek() != first) //don't repeat at the bottom
							scrollPositions.push(first);
					}
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
				}
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (prefs.getBoolean("disableScrollJump", true) == false) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					int last = gv.getLastVisiblePosition();
					if (total == last + 1)
						return true;
					int target = first + (total * app.scrollStep) / 100;
					if (target <= last)
						target = last + 1; // Special for NOOK, otherwise it
											// won't redraw the listview
					if (target > (total - 1))
						target = total - 1;
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (downScroll.hasWindowFocus()) {
					if (prefs.getBoolean("disableScrollJump", true) == false) {
						int first = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						int last = gv.getLastVisiblePosition();
						if (total == last + 1)
							return;
						int target = total - 1;
						RepeatedDownScroll ds = new RepeatedDownScroll();
						ds.doIt(first, target, 0);
					}
				}
			}
		}
		;
		dnScrlSimpleOnGestureListener dnscrl_gl = new dnScrlSimpleOnGestureListener();
		final GestureDetector dnscrl_gd = new GestureDetector(dnscrl_gl);
		downScroll.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				dnscrl_gd.onTouchEvent(event);
				return false;
			}
		});

		refreshBottomInfo();
	}

	private HashMap<String, Drawable> createIconsList(PackageManager pm) {
		Drawable d = null;
		HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent,
				0);
		String pname = "";
		String aname = "";
		String hname = "";
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				pname = ri.activityInfo.packageName;
				aname = ri.activityInfo.name;
				try {
					if (ri.activityInfo.labelRes != 0) {
						hname = (String) ri.activityInfo.loadLabel(pm);
					} else {
						hname = (String) ri.loadLabel(pm);
					}
					if (ri.activityInfo.icon != 0) {
						d = ri.activityInfo.loadIcon(pm);
					} else {
						d = ri.loadIcon(pm);
					}
				} catch (Exception e) {
				}
				if (d != null) {
					rc.put(pname + "%" + aname + "%" + hname, d);
				}
			}
		}
		return rc;
	}

	private static class AppComparator implements java.util.Comparator<String> {
		public int compare(String a, String b) {
			if (a == null && b == null) {
				return 0;
			}
			if (a == null && b != null) {
				return 1;
			}
			if (a != null && b == null) {
				return -1;
			}
			String[] ap = a.split("\\%");
			String[] bp = b.split("\\%");
			return ap[2].compareToIgnoreCase(bp[2]);
		}
	}

	static public List<String> createAppList(PackageManager pm) {
		List<String> rc = new ArrayList<String>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent,
				0);
		String pname = "";
		String aname = "";
		String hname = "";
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				pname = ri.activityInfo.packageName;
				aname = ri.activityInfo.name;
				try {
					if (ri.activityInfo.labelRes != 0) {
						hname = (String) ri.activityInfo.loadLabel(pm);
					} else {
						hname = (String) ri.loadLabel(pm);
					}
				} catch (Exception e) {
				}
				if (!filterMyself || !aname.equals(selfName))
					rc.add(pname + "%" + aname + "%" + hname);
			}
		}
		Collections.sort(rc, new AppComparator());
		return rc;
	}

	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(
						ReLaunch.this,
						getResources().getString(
								R.string.jv_relaunchx_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}

	private BroadcastReceiver SDCardChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Code to react to SD mounted goes here
			Intent i = new Intent(context, ReLaunch.class);
			i.putExtra("home", useHome);
			i.putExtra("home1", useHome1);
			i.putExtra("shop", useShop);
			i.putExtra("library", useLibrary);
			startActivity(i);
		}
	};

	private BroadcastReceiver PowerChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshBottomInfo();
		}
	};

	private BroadcastReceiver WiFiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshBottomInfo();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity relaunchx = this;
		// If we called from Home launcher?
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			useHome = false;
			useHome1 = false;
			useShop = false;
			useLibrary = false;
			useDirViewer = false;
		} else {
			useHome = data.getBooleanExtra("home", false);
			useHome1 = data.getBooleanExtra("home1", false);
			useShop = data.getBooleanExtra("shop", false);
			useLibrary = data.getBooleanExtra("library", false);
			useDirViewer = data.getBooleanExtra("dirviewer", false);
		}

		// Create global storage with values
		app = (ReLaunchApp) getApplicationContext();

		app.FLT_SELECT = getResources().getInteger(R.integer.FLT_SELECT);
		app.FLT_STARTS = getResources().getInteger(R.integer.FLT_STARTS);
		app.FLT_ENDS = getResources().getInteger(R.integer.FLT_ENDS);
		app.FLT_CONTAINS = getResources().getInteger(R.integer.FLT_CONTAINS);
		app.FLT_MATCHES = getResources().getInteger(R.integer.FLT_MATCHES);
		app.FLT_NEW = getResources().getInteger(R.integer.FLT_NEW);
		app.FLT_NEW_AND_READING = getResources().getInteger(
				R.integer.FLT_NEW_AND_READING);

		// Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		String typesString = prefs.getString("types", defReaders);
		try {
			app.scrollStep = Integer.parseInt(prefs.getString("scrollPerc",
					"10"));
			app.viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize",
					"1024"));
			app.editorMax = Integer.parseInt(prefs.getString("editorMaxSize",
					"256"));
		} catch (NumberFormatException e) {
			app.scrollStep = 10;
			app.viewerMax = 1024;
			app.editorMax = 256;
		}
		if (app.scrollStep < 1)
			app.scrollStep = 1;
		if (app.scrollStep > 100)
			app.scrollStep = 100;

		filterMyself = prefs.getBoolean("filterSelf", true);
		if (useHome1 && prefs.getBoolean("homeMode", true))
			useHome = true;
		if (useShop && prefs.getBoolean("shopMode", true))
			useHome = true;
		if (useLibrary && prefs.getBoolean("libraryMode", true))
			useHome = true;
		app.fullScreen = prefs.getBoolean("fullScreen", false);
		app.setFullScreenIfNecessary(this);

		if (app.dataBase == null)
			app.dataBase = new BooksBase(this);
		// Create application icons map
		app.setIcons(createIconsList(getPackageManager()));

		// Create applications label list
		app.setApps(createAppList(getPackageManager()));

		// Readers list
		app.setReaders(parseReadersString(typesString));

		// Miscellaneous lists list
		app.readFile("lastOpened", LRU_FILE);
		app.readFile("favorites", FAV_FILE);
		app.readFile("filters", FILT_FILE, ":");
		app.filters_and = prefs.getBoolean("filtersAnd", true);
		app.readFile("columns", COLS_FILE, ":");
		app.columns.clear();
		for (String[] r : app.getList("columns")) {
			app.columns.put(r[0], Integer.parseInt(r[1]));
		}
		app.readFile("history", HIST_FILE, ":");
		app.history.clear();
		for (String[] r : app.getList("history")) {
			if (r[1].equals("READING"))
				app.history.put(r[0], app.READING);
			else if (r[1].equals("FINISHED"))
				app.history.put(r[0], app.FINISHED);
		}
		setSortMode(prefs.getInt("sortKey", 0), prefs.getInt("sortOrder", 0));
		if (useDirViewer) {
			String start_dir = null;
			setContentView(R.layout.results_layout);
			if (data.getExtras() != null)
				start_dir = data.getStringExtra("start_dir");
			((ImageButton) findViewById(R.id.results_btn))
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							finish();
						}
					});
			if (start_dir != null)
				drawDirectory(start_dir, -1);
		} else {
			// Main layout
			setContentView(R.layout.main);
			if (!prefs.getBoolean("showButtons", true)) {
				hideLayout(R.id.linearLayoutTop);
			}

			if (useHome) {
				app.readFile("app_last", APP_LRU_FILE, ":");
				app.readFile("app_favorites", APP_FAV_FILE, ":");

				final ImageButton lrua_button = ((ImageButton) findViewById(R.id.app_last));
				class LruaSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						Intent intent = new Intent(ReLaunch.this,
								AllApplications.class);
						intent.putExtra("list", "app_last");
						// "Last recently used applications"
						intent.putExtra(
								"title",
								getResources().getString(
										R.string.jv_relaunchx_lru_a));
						startActivity(intent);
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (lrua_button.hasWindowFocus()) {
						}
					}
				}
				;
				LruaSimpleOnGestureListener lrua_gl = new LruaSimpleOnGestureListener();
				final GestureDetector lrua_gd = new GestureDetector(lrua_gl);
				lrua_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						lrua_gd.onTouchEvent(event);
						return false;
					}
				});
				final ImageButton alla_button = ((ImageButton) findViewById(R.id.all_applications_btn));
				class AllaSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						Intent intent = new Intent(ReLaunch.this,
								AllApplications.class);
						intent.putExtra("list", "app_all");
						// "All applications"
						intent.putExtra(
								"title",
								getResources().getString(
										R.string.jv_relaunchx_all_a));
						startActivity(intent);
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (alla_button.hasWindowFocus()) {

						}
					}
				}
				;
				AllaSimpleOnGestureListener alla_gl = new AllaSimpleOnGestureListener();
				final GestureDetector alla_gd = new GestureDetector(alla_gl);
				alla_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						alla_gd.onTouchEvent(event);
						return false;
					}
				});
				final ImageButton fava_button = ((ImageButton) findViewById(R.id.app_favorites));
				class FavaSimpleOnGestureListener extends
						SimpleOnGestureListener {

					private boolean processEvent(String action) {
						if (prefs.getString(action, "RELAUNCH").equals("RELAUNCH")) {
							Intent intent = new Intent(ReLaunch.this, AllApplications.class);
							intent.putExtra("list", "app_favorites");
							intent.putExtra("title", getResources().getString(R.string.jv_relaunchx_fav_a));
							startActivity(intent);
						} else if (prefs.getString(action, "RELAUNCH").equals("RUN")) {
							actionRun(prefs.getString(action + "app", "%%"));
						}
						return true;
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						return processEvent("appFavButtonST");
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return processEvent("appFavButtonDT");
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (ReLaunch.this.hasWindowFocus())
							processEvent("appFavButtonLT");
					}
				};
				FavaSimpleOnGestureListener fava_gl = new FavaSimpleOnGestureListener();
				final GestureDetector fava_gd = new GestureDetector(this, fava_gl);
				fava_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						fava_gd.onTouchEvent(event);
						return false;
					}
				});
			} else {
				//hideLayout(R.id.linearLayoutBottom);
			}

			if (prefs.getBoolean("showButtons", true)) {

				final ImageButton home_button = (ImageButton) findViewById(R.id.home_btn);
				class HomeSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("homeButtonST", "OPENN").equals(
								"OPENN")) {
							openHome(Integer.parseInt(prefs.getString(
									"homeButtonSTopenN", "1")));
						} else if (prefs.getString("homeButtonST", "OPENN")
								.equals("OPENMENU")) {
							menuHome();
						} else if (prefs.getString("homeButtonST", "OPENN")
								.equals("OPENSCREEN")) {
							screenHome();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("homeButtonDT", "OPENMENU").equals(
								"OPENN")) {
							openHome(Integer.parseInt(prefs.getString(
									"homeButtonDTopenN", "1")));
						} else if (prefs.getString("homeButtonDT", "OPENMENU")
								.equals("OPENMENU")) {
							menuHome();
						} else if (prefs.getString("homeButtonDT", "OPENMENU")
								.equals("OPENSCREEN")) {
							screenHome();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (home_button.hasWindowFocus()) {
							if (prefs.getString("homeButtonLT", "OPENSCREEN")
									.equals("OPENN")) {
								openHome(Integer.parseInt(prefs.getString(
										"homeButtonLTopenN", "1")));
							} else if (prefs.getString("homeButtonLT",
									"OPENSCREEN").equals("OPENMENU")) {
								menuHome();
							} else if (prefs.getString("homeButtonLT",
									"OPENSCREEN").equals("OPENSCREEN")) {
								screenHome();
							}
						}
					}
				}
				;
				HomeSimpleOnGestureListener home_gl = new HomeSimpleOnGestureListener();
				final GestureDetector home_gd = new GestureDetector(home_gl);
				home_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						home_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton settings_button = (ImageButton) findViewById(R.id.settings_btn);
				class SettingsSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("settingsButtonST", "RELAUNCH")
								.equals("RELAUNCH")) {
							menuSettings();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("RUN")) {
							actionRun(prefs.getString("settingsButtonSTapp",
									"%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("settingsButtonDT", "RELAUNCH")
								.equals("RELAUNCH")) {
							menuSettings();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("RUN")) {
							actionRun(prefs.getString("settingsButtonDTapp",
									"%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (settings_button.hasWindowFocus()) {
							if (prefs.getString("settingsButtonLT", "RELAUNCH")
									.equals("RELAUNCH")) {
								menuSettings();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("LOCK")) {
								actionLock();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("RUN")) {
								actionRun(prefs.getString(
										"settingsButtonLTapp", "%%"));
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("OPTIONSMENU")) {
								if (Build.VERSION.SDK_INT > 14)
									app.About(relaunchx);
								else
									openOptionsMenu();
							}
						}
					}
				}
				;
				SettingsSimpleOnGestureListener settings_gl = new SettingsSimpleOnGestureListener();
				final GestureDetector settings_gd = new GestureDetector(
						settings_gl);
				settings_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						settings_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton search_button = (ImageButton) findViewById(R.id.search_btn);
				class SearchSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						menuSearch();
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (search_button.hasWindowFocus()) {
						}
					}
				}
				;
				SearchSimpleOnGestureListener search_gl = new SearchSimpleOnGestureListener();
				final GestureDetector search_gd = new GestureDetector(search_gl);
				search_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						search_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton lru_button = (ImageButton) findViewById(R.id.lru_btn);
				class LruSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("lastOpened", Integer.parseInt(prefs
									.getString("lruButtonSTopenN", "1")) - 1);
						} else if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("lastOpened");
						} else if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENSCREEN")) {
							menuLastopened();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("lruButtonDT", "NOTHING").equals(
								"OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("lastOpened", Integer.parseInt(prefs
									.getString("lruButtonDTopenN", "1")) - 1);
						} else if (prefs.getString("lruButtonDT", "NOTHING")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("lastOpened");
						} else if (prefs.getString("lruButtonDT", "NOTHING")
								.equals("OPENSCREEN")) {
							menuLastopened();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (lru_button.hasWindowFocus()) {
							if (prefs.getString("lruButtonLT", "NOTHING")
									.equals("OPENN")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.runItem("lastOpened",
										Integer.parseInt(prefs.getString(
												"lruButtonLTopenN", "1")) - 1);
							} else if (prefs
									.getString("lruButtonLT", "NOTHING")
									.equals("OPENMENU")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.showMenu("lastOpened");
							} else if (prefs
									.getString("lruButtonLT", "NOTHING")
									.equals("OPENSCREEN")) {
								menuLastopened();
							}
						}
					}
				}
				;
				LruSimpleOnGestureListener lru_gl = new LruSimpleOnGestureListener();
				final GestureDetector lru_gd = new GestureDetector(lru_gl);
				lru_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						lru_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton fav_button = (ImageButton) findViewById(R.id.favor_btn);
				class FavSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("favorites", Integer.parseInt(prefs
									.getString("favButtonSTopenN", "1")) - 1);
						} else if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("favorites");
						} else if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENSCREEN")) {
							menuFavorites();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("favButtonDT", "NOTHING").equals(
								"OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("favorites", Integer.parseInt(prefs
									.getString("favButtonDTopenN", "1")) - 1);
						} else if (prefs.getString("favButtonDT", "NOTHING")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("favorites");
						} else if (prefs.getString("favButtonDT", "NOTHING")
								.equals("OPENSCREEN")) {
							menuFavorites();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (fav_button.hasWindowFocus()) {
							if (prefs.getString("favButtonLT", "NOTHING")
									.equals("OPENN")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.runItem("favorites",
										Integer.parseInt(prefs.getString(
												"favButtonLTopenN", "1")) - 1);
							} else if (prefs
									.getString("favButtonLT", "NOTHING")
									.equals("OPENMENU")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.showMenu("favorites");
							} else if (prefs
									.getString("favButtonLT", "NOTHING")
									.equals("OPENSCREEN")) {
								menuFavorites();
							}
						}
					}
				}
				;
				FavSimpleOnGestureListener fav_gl = new FavSimpleOnGestureListener();
				final GestureDetector fav_gd = new GestureDetector(fav_gl);
				fav_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						fav_gd.onTouchEvent(event);
						return false;
					}
				});
			}

			// Memory buttons (task manager activity)
			final RelativeLayout mem_l = (RelativeLayout) findViewById(R.id.mem_layout);
			if (mem_l != null) {
				class MemlSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("memButtonST", "RELAUNCH").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(ReLaunch.this,
									TaskManager.class);
							startActivity(intent);
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("RUN")) {
							actionRun(prefs.getString("memButtonSTapp", "%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("memButtonDT", "NOTHING").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(ReLaunch.this,
									TaskManager.class);
							startActivity(intent);
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("RUN")) {
							actionRun(prefs.getString("memButtonDTapp", "%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (mem_l.hasWindowFocus()) {
							if (prefs.getString("memButtonLT", "NOTHING")
									.equals("RELAUNCH")) {
								Intent intent = new Intent(ReLaunch.this,
										TaskManager.class);
								startActivity(intent);
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("LOCK")) {
								actionLock();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("RUN")) {
								actionRun(prefs.getString("memButtonLTapp",
										"%%"));
							}
						}
					}
				}
				;
				MemlSimpleOnGestureListener meml_gl = new MemlSimpleOnGestureListener();
				final GestureDetector meml_gd = new GestureDetector(meml_gl);
				mem_l.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						meml_gd.onTouchEvent(event);
						return false;
					}
				});
			}
			memLevel = (TextView) findViewById(R.id.mem_level);
			memTitle = (TextView) findViewById(R.id.mem_title);

			// Battery Layout
			final RelativeLayout bat_l = (RelativeLayout) findViewById(R.id.bat_layout);
			if (bat_l != null) {
				class BatlSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("batButtonST", "RELAUNCH").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(
									Intent.ACTION_POWER_USAGE_SUMMARY);
							startActivity(intent);
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("RUN")) {
							actionRun(prefs.getString("batButtonSTapp", "%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("batButtonDT", "NOTHING").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(
									Intent.ACTION_POWER_USAGE_SUMMARY);
							startActivity(intent);
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("RUN")) {
							actionRun(prefs.getString("batButtonDTapp", "%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (mem_l.hasWindowFocus()) {
							if (prefs.getString("batButtonLT", "NOTHING")
									.equals("RELAUNCH")) {
								Intent intent = new Intent(
										Intent.ACTION_POWER_USAGE_SUMMARY);
								startActivity(intent);
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("LOCK")) {
								actionLock();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("RUN")) {
								actionRun(prefs.getString("batButtonLTapp",
										"%%"));
							}
						}
					}
				}
				;
				BatlSimpleOnGestureListener batl_gl = new BatlSimpleOnGestureListener();
				final GestureDetector batl_gd = new GestureDetector(batl_gl);
				bat_l.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						batl_gd.onTouchEvent(event);
						return false;
					}
				});
			}
			// Battery buttons
			battLevel = (TextView) findViewById(R.id.bat_level);
			battTitle = (TextView) findViewById(R.id.bat_title);
			batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

			// What's new processing
			final int latestVersion = prefs.getInt("latestVersion", 0);
			int tCurrentVersion = 0;
			try {
				tCurrentVersion = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;
			} catch (Exception e) {
			}
			final int currentVersion = tCurrentVersion;
			if (currentVersion > latestVersion) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				WebView wv = new WebView(this);
				wv.loadDataWithBaseURL(null,
						getResources().getString(R.string.about_help)
								+ getResources().getString(R.string.about_appr)
								+ getResources().getString(R.string.whats_new),
						"text/html", "utf-8", null);
				// "What's new"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_whats_new));
				builder.setView(wv);
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunchx_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SharedPreferences.Editor editor = prefs.edit();
								editor.putInt("latestVersion", currentVersion);
								editor.commit();
								dialog.dismiss();
							}
						});
				builder.show();
			}

			// incorrect device warning
			checkDevice(Build.DEVICE, Build.MANUFACTURER, Build.MODEL,
					Build.PRODUCT);

			setEinkController();

			// First directory to get to
			currentPosition = -1;
			if (data.getExtras() != null && data.getExtras().getString("start_dir") != null) {
				currentRoot = data.getExtras().getString("start_dir");
			} else {
				String lastDirPath = prefs.getString("lastdir", "/sdcard");
				File lastDir = new File(lastDirPath);
				if (prefs.getBoolean("saveDir", true) && lastDir.exists())
					currentRoot = lastDirPath;
				else {
					String[] startDirs = prefs.getString("startDir","/sdcard,/media/My Files").split("\\,");
					boolean dirFound = false;
					for (String dir : startDirs) if (new File(dir).exists()) {
						currentRoot = dir;
						dirFound = true;
						break;
					}
					if (dirFound == false) {
						currentRoot ="/";
					}
				}
			}
		}

		app.booted = true;

		if (!mountReceiverRegistered) {
			IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
			filter.addDataScheme("file");
			registerReceiver(this.SDCardChangeReceiver,
					new IntentFilter(filter));
			mountReceiverRegistered = true;
		}

		if (!powerReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_POWER_CONNECTED);
			filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(this.PowerChangeReceiver, new IntentFilter(filter));
			powerReceiverRegistered = true;
		}

		if (!wifiReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(this.WiFiChangeReceiver, new IntentFilter(filter));
			wifiReceiverRegistered = true;
		}

		ScreenOrientation.set(this, prefs);

		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, findViewById(R.id.linearLayoutTop));
		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, findViewById(R.id.title_txt));
		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, findViewById(R.id.linearLayoutNavigate));
		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, findViewById(R.id.linearLayoutBottom));
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (app.dataBase == null)
			app.dataBase = new BooksBase(this);

		// Reread preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String typesString = prefs.getString("types", defReaders);
		// Recreate readers list
		app.setReaders(parseReadersString(typesString));
		app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
		drawDirectory(currentRoot, currentPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			menuSearch();
			return true;
		case R.id.mime_types:
			menuTypes();
			return true;
		case R.id.about:
			menuAbout();
			return true;
		case R.id.setting:
			menuSettings();
			return true;
		case R.id.lastopened:
			menuLastopened();
			return true;
		case R.id.favorites:
			menuFavorites();
			return true;
		default:
			return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = createReadersString(app.getReaders());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();
			drawDirectory(currentRoot, currentPosition);
			break;
		default:
			return;
		}
	}

	public boolean onContextMenuSelected(int itemId, int mPos) {
		if (itemId == CNTXT_MENU_CANCEL)
			return true;
		FileDetails i;
		int tpos = 0;
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
//				.getMenuInfo();
		if (mPos == -1) {
			i = new FileDetails();
			i.directoryName = prefs.getString("lastdir", ".");
			i.name = "";
		} else {
			tpos = mPos;
			i = itemsArray.get(tpos);
		}
		final int pos = tpos;
		final String fname = i.name;
		final String dname = i.directoryName;
		final String fullName = dname + File.separator + fname;
		final FsItemType type = i.type;

		switch (itemId) {
		case CNTXT_MENU_SET_STARTDIR:
			app.setStartDir(fullName);
			drawDirectory(fullName, -1);
			break;
		case CNTXT_MENU_ADD_STARTDIR:
			app.addStartDir(fullName);
			break;
		case CNTXT_MENU_ADD:
			if (type == FsItemType.File)
				app.addToList("favorites", dname, fname, false);
			else
				app.addToList("favorites", fullName, app.DIR_TAG, false);
			break;
		case CNTXT_MENU_MARK_READING:
			app.history.put(fullName, app.READING);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_MARK_FINISHED:
			app.history.put(fullName, app.FINISHED);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_MARK_FORGET:
			app.history.remove(fullName);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_OPENWITH: {
			final CharSequence[] applications = app.getApps().toArray(
					new CharSequence[app.getApps().size()]);
			CharSequence[] happlications = app.getApps().toArray(
					new CharSequence[app.getApps().size()]);
			for (int j = 0; j < happlications.length; j++) {
				String happ = (String) happlications[j];
				String[] happp = happ.split("\\%");
				happlications[j] = happp[2];
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select application"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_select_application));
			builder.setSingleChoiceItems(happlications, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							start(app.launchReader((String) applications[i],
									fullName));
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			builder.show();
			break;
		}
		case CNTXT_MENU_INTENT: {
			String re[] = fname.split("\\.");
			List<String> ilist = new ArrayList<String>();
			for (int j = 1; j < re.length; j++) {
				String act = "application/";
				String typ = re[j];
				if (typ.equals("jpg"))
					typ = "jpeg";
				if (typ.equals("jpeg") || typ.equals("png"))
					act = "image/";
				ilist.add(act + typ);
				if (re.length > 2) {
					for (int k = j + 1; k < re.length; k++) {
						String x = "";
						for (int l = k; l < re.length; l++)
							x += "+" + re[l];
						ilist.add(act + typ + x);
					}
				}
			}

			final CharSequence[] intents = ilist.toArray(new CharSequence[ilist
					.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select intent type"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_select_intent_type));
			builder.setSingleChoiceItems(intents, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							Intent in = new Intent();
							in.setAction(Intent.ACTION_VIEW);
							in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TOP);
							in.setDataAndType(Uri.parse("file://" + fullName),
									(String) intents[i]);
							dialog.dismiss();
							try {
								startActivity(in);
							} catch (ActivityNotFoundException e) {
								AlertDialog.Builder builder1 = new AlertDialog.Builder(
										ReLaunch.this);
								// "Activity not found"
								builder1.setTitle(getResources()
										.getString(
												R.string.jv_relaunchx_activity_not_found_title));
								// "Activity for file \"" + fullName +
								// "\" with type \"" + intents[i] +
								// "\" not found"
								builder1.setMessage(getResources()
										.getString(
												R.string.jv_relaunchx_activity_not_found_text1)
										+ " \""
										+ fullName
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunchx_activity_not_found_text2)
										+ " \""
										+ intents[i]
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunchx_activity_not_found_text3));
								builder1.setPositiveButton(getResources()
										.getString(R.string.jv_relaunchx_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
											}
										});
								builder1.show();
							}
						}
					});
			// "Other"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunchx_other),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							AlertDialog.Builder builder1 = new AlertDialog.Builder(
									ReLaunch.this);
							// "Intent type"
							builder1.setTitle(getResources().getString(
									R.string.jv_relaunchx_intent_type));
							final EditText input = new EditText(ReLaunch.this);
							input.setText("application/");
							builder1.setView(input);
							// "Ok"
							builder1.setPositiveButton(getResources()
									.getString(R.string.jv_relaunchx_ok),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											Intent in = new Intent();
											in.setAction(Intent.ACTION_VIEW);
											in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_CLEAR_TOP);
											in.setDataAndType(
													Uri.parse("file://"
															+ fullName), input
															.getText()
															.toString());
											dialog.dismiss();
											try {
												startActivity(in);
											} catch (ActivityNotFoundException e) {
												AlertDialog.Builder builder2 = new AlertDialog.Builder(
														ReLaunch.this);
												// "Activity not found"
												builder2.setTitle(getResources()
														.getString(
																R.string.jv_relaunchx_activity_not_found_title));
												// "Activity for file \"" +
												// fullName + "\" with type \""
												// + input.getText() +
												// "\" not found"
												builder2.setMessage(getResources()
														.getString(
																R.string.jv_relaunchx_activity_not_found_text1)
														+ " \""
														+ fullName
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunchx_activity_not_found_text2)
														+ " \""
														+ input.getText()
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunchx_activity_not_found_text3));
												// "OK"
												builder2.setPositiveButton(
														getResources()
																.getString(
																		R.string.jv_relaunchx_ok),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {
															}
														});
												builder2.show();
											}
										}
									});
							builder1.show();
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			break;
		}
		case CNTXT_MENU_DELETE_F:
			if (prefs.getBoolean("confirmFileDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete file warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_del_file_title));
				// "Are you sure to delete file \"" + fullPathName + "\" ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunchx_del_file_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunchx_del_file_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunchx_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeFile(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunchx_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeFile(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;
		case CNTXT_MENU_DELETE_D_EMPTY:
			if (prefs.getBoolean("confirmDirDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete empty directory warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_del_em_dir_title));
				// "Are you sure to delete empty directory \"" + fullPathName + "\" ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunchx_del_em_dir_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunchx_del_em_dir_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunchx_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeFile(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunchx_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeFile(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;
		case CNTXT_MENU_DELETE_D_NON_EMPTY:
			if (prefs.getBoolean("confirmNonEmptyDirDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete non empty directory warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_del_ne_dir_title));
				// "Are you sure to delete non-empty directory \"" + fullPathName +
				// "\" (dangerous) ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunchx_del_ne_dir_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunchx_del_ne_dir_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunchx_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeDirectory(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunchx_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeDirectory(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;

		case CNTXT_MENU_COPY_FILE:
			fileOpFile = fname;
			fileOpDir = dname;
			fileOp = CNTXT_MENU_COPY_FILE;
			break;

		case CNTXT_MENU_MOVE_FILE:
			fileOpFile = fname;
			fileOpDir = dname;
			fileOp = CNTXT_MENU_MOVE_FILE;
			break;

		case CNTXT_MENU_MOVE_DIR:
			fileOpFile = fname;
			fileOpDir = dname;
			fileOp = CNTXT_MENU_MOVE_DIR;
			break;

		case CNTXT_MENU_PASTE:
			String src;
			if (fileOpDir.equalsIgnoreCase("/"))
				src = fileOpDir + fileOpFile;
			else
				src = fileOpDir + "/" + fileOpFile;
			String dst = dname + "/" + fileOpFile;
			boolean retCode = false;
			if (fileOp == CNTXT_MENU_COPY_FILE)
				retCode = app.copyFile(src, dst, false);
			else if ((fileOp == CNTXT_MENU_MOVE_FILE) || (fileOp == CNTXT_MENU_MOVE_DIR))
				retCode = app.moveFile(src, dst);
			if (retCode) {
				FileDetails fitem = new FileDetails();
				fitem.name = fileOpFile;
				fitem.directoryName = dname;
				fitem.fullPathName = dname + File.separator + fileOpFile;
				if ((fileOp == CNTXT_MENU_MOVE_FILE) || (fileOp == CNTXT_MENU_COPY_FILE)) {
                    String[] fparts = fileOpFile.split("[.]");
                    fitem.extension = fparts.length > 1 ? fparts[fparts.length -1] : "";
                    fitem.type = FsItemType.File;
					fitem.reader = app.readerName(fileOpFile);
					if (prefs.getBoolean("showBookTitles", false))
						fitem.displayName = getEbookName(dname, fileOpFile);
					else
                        fitem.displayName = fileOpFile;
				} else if (fileOp == CNTXT_MENU_MOVE_DIR) {
					fitem.displayName = fileOpFile;
					fitem.type = FsItemType.Directory;
					fitem.reader = "nope";
				}
				itemsArray.add(fitem);
				fileOp = 0;
//				redrawList();
				drawDirectory(dname, currentPosition);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(
						R.string.jv_relaunchx_error_title));
				builder.setMessage(getResources().getString(
						R.string.jv_relaunchx_paste_fail_text)
						+ " " + fileOpFile);
				builder.setNeutralButton(
						getResources().getString(R.string.jv_relaunchx_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			}
			break;

		case CNTXT_MENU_TAGS_RENAME: {
			final Context mThis = this;
			final String oldFullName = dname + "/" + fname;
			String newName = getEbookName(dname, fname);
			newName = newName.replaceAll("[\n\r]", ". ");
			if (fname.endsWith("fb2"))
				newName = newName.concat(".fb2");
			else if (fname.endsWith("fb2.zip"))
				newName = newName.concat(".fb2.zip");
			else if (fname.endsWith("epub"))
				newName = newName.concat(".epub");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(newName);
			builder.setView(input);
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_rename_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunchx_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String newName = input.getText().toString().trim();
							String newFullName = dname + "/" + newName;
							if (app.moveFile(oldFullName, newFullName)) {
								itemsArray.get(pos).name = newName;
								itemsArray.get(pos).displayName = newName;
								itemsArray.get(pos).fullPathName = newFullName;
								drawDirectory(dname, currentPosition);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(
										R.string.jv_relaunchx_error_title));
								builder.setMessage(getResources().getString(
										R.string.jv_relaunchx_rename_fail_text)
										+ " " + fname);
								builder.setNeutralButton(
										getResources().getString(R.string.jv_relaunchx_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
												dialog.dismiss();
											}
										});
								builder.show();
							}
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_RENAME: {
			final Context mThis = this;
			final String oldFullName = dname + "/" + fname;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(fname);
			input.selectAll();
			builder.setView(input);
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_rename_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunchx_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String newName = input.getText().toString().trim();
							String newFullName = dname + "/" + newName;
							if (app.moveFile(oldFullName, newFullName)) {
								itemsArray.get(pos).name = newName;
								itemsArray.get(pos).displayName = newName;
								itemsArray.get(pos).fullPathName = newFullName;
								drawDirectory(dname, currentPosition);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(
										R.string.jv_relaunchx_error_title));
								builder.setMessage(getResources().getString(
										R.string.jv_relaunchx_rename_fail_text)
										+ " " + fname);
								builder.setNeutralButton(
										getResources().getString(R.string.jv_relaunchx_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
												dialog.dismiss();
											}
										});
								builder.show();
							}
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_CREATE_DIR: {
			final Context mThis = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			builder.setView(input);
			builder.setTitle(getResources().getString(
					R.string.jv_relaunchx_create_folder_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunchx_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String dname = prefs.getString("lastdir", ".");
							String newName = input.getText().toString().trim();
							if (newName.equalsIgnoreCase("")) {
								return;
							}
							String newFullName = dname + "/" + newName;
							if (app.createDir(newFullName)) {
								FileDetails fitem = new FileDetails();
								fitem.name = newName;
								fitem.displayName = newName;
								fitem.directoryName = dname;
								fitem.fullPathName = newFullName;
								fitem.type = FsItemType.Directory;
								fitem.reader = "nope";
								itemsArray.add(fitem);
//								redrawList();
								drawDirectory(dname, currentPosition);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(
										R.string.jv_relaunchx_error_title));
								builder.setMessage(getResources().getString(
										R.string.jv_relaunchx_create_folder_fail_text)
										+ " " + newFullName);
								builder.setNeutralButton(
										getResources().getString(R.string.jv_relaunchx_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
												dialog.dismiss();
											}
										});
								builder.show();
							}
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunchx_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_SWITCH_TITLES:
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("showBookTitles", !prefs.getBoolean("showBookTitles", false));
			editor.commit();
			drawDirectory(dname, currentPosition);
			break;

		case CNTXT_MENU_SHOW_BOOKINFO:
			showBookInfo(dname + "/" + fname);
			break;

		case CNTXT_MENU_FILE_INFO:
			showFileInfo(dname + "/" + fname);
			break;

		}
		return true;
	}

	@Override
	protected void onResume() {
		setEinkController();
		super.onResume();
		if (app.dataBase == null)
			app.dataBase = new BooksBase(this);
		app.generalOnResume(TAG, this);
		refreshBottomInfo();
		redrawList();

		//It has to be here to anyhow start the service upon device boot.
		//I have tried with BOOT_COMPLETED broadcast receiver but it does not work on Onyx :(
		//The drawback is that I don't know how else I can lock the screen upon boot, I cannot do it
		//here obviosly.
		startService(new Intent(this, LockScreen.class));
	}

	@Override
	protected void onStop() {

		int lruMax = 30;
		int favMax = 30;
		int appLruMax = 30;
		int appFavMax = 30;
		try {
			lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
			favMax = Integer.parseInt(prefs.getString("favSize", "30"));
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
			appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("lastOpened", LRU_FILE, lruMax);
		app.writeFile("favorites", FAV_FILE, favMax);
		app.writeFile("app_last", APP_LRU_FILE, appLruMax, ":");
		app.writeFile("app_favorites", APP_FAV_FILE, appFavMax, ":");
		List<String[]> h = new ArrayList<String[]>();
		for (String k : app.history.keySet()) {
			if (app.history.get(k) == app.READING)
				h.add(new String[] { k, "READING" });
			else if (app.history.get(k) == app.FINISHED)
				h.add(new String[] { k, "FINISHED" });
		}
		app.setList("history", h);
		app.writeFile("history", HIST_FILE, 0, ":");
		List<String[]> c = new ArrayList<String[]>();
		for (String k : app.columns.keySet()) {
			c.add(new String[] { k, Integer.toString(app.columns.get(k)) });
		}
		app.setList("columns", c);
		app.writeFile("columns", ReLaunch.COLS_FILE, 0, ":");
		// unregisterReceiver(this.SDReceiver);
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isRoot = false;
		if (!useHome)
			return super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_HOME)
			return true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			String[] rootDirs = prefs.getString("startDir", "/sdcard").split(",");
			for (int i = 0; i < rootDirs.length; i++) {
				if (rootDirs[i].equalsIgnoreCase(currentRoot))
					isRoot = true;
			}
			if (currentRoot.equalsIgnoreCase("/"))
				isRoot = true;
			if (!isRoot) {
				String newRoot = currentRoot.substring(0, currentRoot.lastIndexOf("/"));
				drawDirectory(newRoot, -1);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void menuSearch() {
		Intent intent = new Intent(ReLaunch.this, SearchActivity.class);
		startActivity(intent);
	}

	private void menuTypes() {
		Intent intent = new Intent(ReLaunch.this, TypesActivity.class);
		startActivityForResult(intent, TYPES_ACT);
	}

	private void menuSettings() {
		Intent intent = new Intent(ReLaunch.this, PrefsActivity.class);
		startActivity(intent);
	}

	private void menuLastopened() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "lastOpened");
		// "Last opened"
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunchx_lru));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void menuFavorites() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "favorites");
		// "Favorites"
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunchx_fav));
		intent.putExtra("rereadOnStart", true);
		setEinkController(); // ??? not needed
		startActivity(intent);
	}

	private void openHome(Integer order_num) {
		String[] startDirs = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		if (order_num > 0 && order_num <= startDirs.length) {
			drawDirectory(startDirs[order_num - 1], -1);
		}
	}

	private void menuHome() {
		final String[] homesList = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		final CharSequence[] hhomes = new CharSequence[homesList.length];
		for (int j = 0; j < homesList.length; j++) {
			int ind = homesList[j].lastIndexOf('/');
			if (ind == -1) {
				hhomes[j] = "";
			} else {
				hhomes[j] = homesList[j].substring(ind + 1);
				if (hhomes[j].equals("")) {
					hhomes[j] = "/";
				}
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		// "Select home directory"
		builder.setTitle(R.string.jv_relaunchx_home);
		builder.setSingleChoiceItems(hhomes, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						drawDirectory(homesList[i], -1);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(
				getResources().getString(R.string.jv_relaunchx_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void screenHome() {
		// make home list
		List<String[]> homeList = new ArrayList<String[]>();
		String[] startDirs = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		for (Integer i = 0; i < startDirs.length; i++) {
			String[] homeEl = new String[2];
			homeEl[0] = startDirs[i];
			homeEl[1] = app.DIR_TAG;
			homeList.add(homeEl);
		}
		app.setList("homeList", homeList);
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "homeList");
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunchx_home));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void menuAbout() {
		app.About(this);
	}

	private Integer getDirectoryColumns(String dir) {
		Integer columns = 0;
		if (app.columns.containsKey(dir)) {
			columns = app.columns.get(dir);
		}
		return columns;
	}
	
	private void hideLayout(int id) {
		View v = (View) findViewById(id);
		LayoutParams p = (LayoutParams) v.getLayoutParams();
		p.height = 0;
		p.width = 0;
		v.setLayoutParams(p);
	}

	private List<FileDetails> sortFiles(List<FileDetails> list, SortKey sortKey, SortMode sortMode) {
		class FileDetailsComparator implements Comparator<Object> {
			SortKey primaryKey = null;
            SortKey secondaryKey = null;
            SortMode mode = null;

			FileDetailsComparator(SortKey primarykey, SortMode mode) {
				this.mode = mode;
				this.primaryKey = primarykey;
                if (primarykey == SortKey.FileExtension
                        || primarykey == SortKey.FileDate
                        || primarykey == SortKey.FileSize) {
                    this.secondaryKey = SortKey.FileName;
                }
			}

			private int compareProperty(FileDetails lhs, FileDetails rhs, SortKey sortKey, SortMode sortMode) {
				int ret = 0;
                switch(sortKey) {
                    case BookTitle:
                        ret = lhs.displayName.compareToIgnoreCase(rhs.displayName);
                        break;
                    case FileName:
                        ret = lhs.name.compareToIgnoreCase(rhs.name);
                        break;
                    case FileExtension:
                        ret = lhs.extension.compareToIgnoreCase(rhs.extension);
                        break;
                    case FileDate:
                        ret = lhs.date.compareTo(rhs.date);
                        break;
                    case FileSize:
                        ret = ((Long)lhs.size).compareTo((Long)rhs.size);
                        break;
                    default:
                        Log.e("FileDitailsCompare", "Comparator not implemented for mode: " + sortMode);
                }
                return (sortMode == SortMode.Ascending)? ret : ret * -1;
			}

			public int compare(Object lhs, Object rhs) {
                int eq = compareProperty((FileDetails) lhs, (FileDetails) rhs, primaryKey, mode);
                if (eq == 0 && secondaryKey != null) {
                    eq = compareProperty((FileDetails) lhs, (FileDetails) rhs, secondaryKey, SortMode.Ascending);
                }

                return eq;
			}
		}
		FileDetailsComparator comparator = new FileDetailsComparator(sortKey, sortMode);
		Collections.sort(list, comparator);
		return list;
	}

	private void menuSort() {
		final String[] orderList;
		final int currentSortKey[] = new int[1];

		if (prefs.getBoolean("showBookTitles", false)) {
			orderList = new String[2];
            orderList[0] = getString(R.string.jv_relaunchx_sort_title);
			orderList[1] = getString(R.string.jv_relaunchx_sort_file);
		} else {
			orderList = new String[4];
			orderList[0] = getString(R.string.jv_relaunchx_sort_file);
			orderList[1] = getString(R.string.jv_relaunchx_sort_extension);
			orderList[2] = getString(R.string.jv_relaunchx_sort_size);
            orderList[3] = getString(R.string.jv_relaunchx_sort_date);
		}
		int sortKey = prefs.getInt("sortKey", 0);
		if (sortKey > orderList.length - 1)
			sortKey = 0;
		currentSortKey[0] = sortKey;

		DialogInterface.OnClickListener onApplySorting = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor editor = prefs.edit();
				int sortOrder = 1;
				if (which == -1)
					sortOrder = 0; //Ascending
				editor.putInt("sortOrder", sortOrder);
				editor.putInt("sortKey", currentSortKey[0]);
				editor.commit();
				setSortMode(currentSortKey[0], sortOrder);
				dialog.dismiss();
				drawDirectory(currentRoot, -1);
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		builder.setTitle(R.string.jv_relaunchx_sort_header);
		builder.setSingleChoiceItems(orderList, currentSortKey[0],
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						currentSortKey[0] = i;
					}
				});
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunchx_sort_asc),
						onApplySorting);
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunchx_sort_dsc),
						onApplySorting);
		builder.show();
	}

	@Override
	public void onDestroy() {
//		app.dataBase.db.close();
		unregisterReceiver(this.SDCardChangeReceiver);
		unregisterReceiver(this.PowerChangeReceiver);
		unregisterReceiver(this.WiFiChangeReceiver);
		wifiReceiverRegistered = false;
		powerReceiverRegistered = false;
		mountReceiverRegistered = false;
		super.onDestroy();
	}

	private void setSortMode(int key, int mode) {
		if ((!prefs.getBoolean("showBookTitles", false))) {
			key += 1; //Skip stort by Book Title enum value.
            if (key > 3) {
                Log.e("SortKey", "Index outside of enum: ShowBookTitles=false, index " + key);
                key = 0;
            }
        } else {
            if (key > 1) {
                Log.e("SortKey", "Index outside of enum: ShowBookTitle=true, index " + key);
                key = 0;
            }
        }
        if (mode > 1) {
			Log.e("SortOrder", "Index outside of enum: index " + mode);
			mode = 0;
		}
        sortKey = SortKey.values()[key];
        sortMode = SortMode.values()[mode];
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		class RepeatedDownScroll {
			public void doIt(int first, int target, int shift) {
				final GridView gv = (GridView) findViewById(R.id.gl_list);
				int total = gv.getCount();
				int last = gv.getLastVisiblePosition();
				if (total == last + 1)
					return;
				final int ftarget = target + shift;
				gv.clearFocus();
				gv.post(new Runnable() {
					public void run() {
						gv.setSelection(ftarget);
					}
				});
				final int ffirst = first;
				final int fshift = shift;
				gv.postDelayed(new Runnable() {
					public void run() {
						int nfirst = gv.getFirstVisiblePosition();
						if (nfirst == ffirst) {
							RepeatedDownScroll ds = new RepeatedDownScroll();
							ds.doIt(ffirst, ftarget, fshift + 1);
						}
					}
				}, 150);
			}
		}

		if (DeviceInfo.EINK_SONY) {
			int prevCode = 0x0069;
			int nextCode = 0x006a;
			GridView gv = (GridView) findViewById(R.id.gl_list);
			if ((event.getScanCode() == prevCode) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
				int first = gv.getFirstVisiblePosition();
				int visible = gv.getLastVisiblePosition()
						- gv.getFirstVisiblePosition() + 1;
				int total = itemsArray.size();
				first -= visible;
				if (first < 0)
					first = 0;
				gv.setSelection(first);
				// some hack workaround against not scrolling in some cases
				if (total > 0) {
					gv.requestFocusFromTouch();
					gv.setSelection(first);
				}
			}
			if ((event.getScanCode() == nextCode) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
				int first = gv.getFirstVisiblePosition();
				int total = itemsArray.size();
				int last = gv.getLastVisiblePosition();
				if (total == last + 1)
					return true;
				int target = last + 1;
				if (target > (total - 1))
					target = total - 1;
				RepeatedDownScroll ds = new RepeatedDownScroll();
				ds.doIt(first, target, 0);
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void showBookInfo(String file) {
		final int COVER_MAX_W = 280;
		Bitmap cover = null;
		final Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		dialog.setContentView(R.layout.bookinfo);
		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, dialog.findViewById(R.id.linearLayoutTop));

		Parser parser = new InstantParser();
		EBook eBook = parser.parse(file, true);
		if (eBook.cover != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(eBook.cover, 0,
					eBook.cover.length);
			if (bitmap != null) {
				int width = Math.min(COVER_MAX_W, bitmap.getWidth());
				int height = (int) (width * bitmap.getHeight())/bitmap.getWidth();
				 cover = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
		}

		if (eBook.isOk) {
			ImageView img = (ImageView) dialog.findViewById(R.id.cover);
			if (cover != null)
				img.setImageBitmap(cover);
			else
				img.setVisibility(View.GONE);
			TextView tv = (TextView) dialog.findViewById(R.id.tvTitle);
			tv.setText(eBook.title);
			tv = (TextView) dialog.findViewById(R.id.tvAnnotation);
			if (eBook.annotation != null) {
				eBook.annotation = eBook.annotation.trim()
					.replace("<p>", "")
					.replace("</p>", "\n");
				tv.setText(eBook.annotation);
			} else
				tv.setVisibility(View.GONE);
			ListView lv = (ListView) dialog.findViewById(R.id.authors);
			lv.setDivider(null);
			if (eBook.authors.size() > 0) {
				final String[] authors = new String[eBook.authors.size()];
				for (int i = 0; i < eBook.authors.size(); i++) {
					String author = "";
					if (eBook.authors.get(i).firstName != null)
						if (eBook.authors.get(i).firstName.length() > 0)
							author += eBook.authors.get(i).firstName.substring(0,1) + ".";
					if (eBook.authors.get(i).middleName != null)
						if (eBook.authors.get(i).middleName.length() > 0)
							author += eBook.authors.get(i).middleName.substring(0,1) + ".";
					if (eBook.authors.get(i).lastName != null)
						author += " " + eBook.authors.get(i).lastName;
					authors[i] = author;
				}
				final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, authors);
				lv.setAdapter(lvAdapter);
			}
			tv = (TextView) dialog.findViewById(R.id.tvSeries);
			if (eBook.sequenceName != null) {
				tv.setText(eBook.sequenceName);
			}
			
			((TextView) dialog.findViewById(R.id.book_title)).setText(file.substring(file.lastIndexOf("/") + 1));
		}

		ImageButton btn = (ImageButton) dialog.findViewById(R.id.btnExit);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		SizeManipulation.AdjustWithPreferencesToolbarMinHeight(app, prefs, findViewById(R.id.linearLayoutTop));
		dialog.show();
	}

	private void showFileInfo(String filename) {
		File file = new File(filename);

		final Dialog dialog = new Dialog(this);
		if (file.isDirectory()) {
			dialog.setTitle(getString(R.string.jv_relaunchx_fileinfo_title2));
		} else {
			dialog.setTitle(getString(R.string.jv_relaunchx_fileinfo_title));
		}
		dialog.setContentView(R.layout.fileinfo);

		LinearLayout llSize = (LinearLayout) dialog.findViewById(R.id.llSize);
		if (file.isDirectory())
			llSize.setVisibility(View.GONE);
		TextView tv = (TextView) dialog.findViewById(R.id.tvName);
		tv.setText(file.getName());
		tv = (TextView) dialog.findViewById(R.id.tvSize);
		tv.setText(FileSystem.bytesToString(file.length()) + " (" + file.length() + " bytes)");
		tv = (TextView) dialog.findViewById(R.id.tvTime);
		tv.setText((new Date(file.lastModified())).toLocaleString());
        if (DeviceInfo.isRooted()) {
            String fileAttr = null;
            try {
                Runtime rt = Runtime.getRuntime();
                String[] args = {"ls", "-l", filename};
                Process proc = rt.exec(args);
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = br.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                br.close();
                proc.waitFor();
                fileAttr = output.toString();
            } catch (Throwable t) {
            }
            if (!"".equals(fileAttr)) {
                fileAttr = fileAttr.replaceAll(" +", " ");
                int iPerm = fileAttr.indexOf(" ");
                int iOwner = fileAttr.indexOf(" ", iPerm + 1);
                int iGroup = fileAttr.indexOf(" ", iOwner + 1);
                tv = (TextView) dialog.findViewById(R.id.tvPerm);
                tv.setText(fileAttr.substring(1, iPerm));
                tv = (TextView) dialog.findViewById(R.id.tvOwner);
                tv.setText(fileAttr.substring(iPerm + 1, iOwner) + "/" + fileAttr.substring(iOwner + 1, iGroup));
            }
        } else {
            dialog.findViewById(R.id.tvPerm).setVisibility(View.GONE);
            dialog.findViewById(R.id.tvPermLabel).setVisibility(View.GONE);
            dialog.findViewById(R.id.tvOwner).setVisibility(View.GONE);
            dialog.findViewById(R.id.tvOwnerLabel).setVisibility(View.GONE);
        }
		Button btn = (Button) dialog.findViewById(R.id.btnOk);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

    public String getEbookName(String dir, String file) {
        String format = prefs.getString("bookTitleFormat", "%t[\n%a][. %s][-%n]");
        String fileName = dir + File.separator + file;
        EBook eBook;
        if ((!file.endsWith("fb2")) && (!file.endsWith("fb2.zip"))
                && (!file.endsWith("epub")))
            return file;
        eBook = app.dataBase.getBookByFileName(fileName);
        if (!eBook.isOk) {
            Parser parser = new InstantParser();
            eBook = parser.parse(fileName);
            if (eBook.isOk) {
                if ((eBook.sequenceNumber != null)
                        && (eBook.sequenceNumber.length() == 1))
                    eBook.sequenceNumber = "0" + eBook.sequenceNumber;
                app.dataBase.addBook(eBook);
            }
        }
        if (eBook.isOk) {
            String output = format;
            if (eBook.authors.size() > 0) {
                String author = "";
                if (eBook.authors.get(0).firstName != null)
                    author += eBook.authors.get(0).firstName;
                if (eBook.authors.get(0).lastName != null)
                    author += " " + eBook.authors.get(0).lastName;
                if (author.trim().compareTo("") != 0)
                    output = output.replace("%a", author);
                else
                    output = output.replace("%a", getResources().getString(R.string.jv_bookbase_noauthor));
            } else {
                output = output.replace("%a", getResources().getString(R.string.jv_bookbase_noauthor));
            }

            if (eBook.title != null)
                output = output.replace("%t", eBook.title);
            else
                output = output.replace("%t", getResources().getString(R.string.jv_bookbase_notitle));
            if (eBook.sequenceName != null)
                output = output.replace("%s", eBook.sequenceName);
            else
                output = output.replace("%s", "");
            if (eBook.sequenceNumber != null)
                output = output.replace("%n", eBook.sequenceNumber);
            else
                output = output.replace("%n", "");
            output = output.replace("%f", fileName);
            Pattern purgeBracketsPattern = Pattern.compile("\\[[\\s\\.\\-_]*\\]");
            output = purgeBracketsPattern.matcher(output).replaceAll("");
            output = output.replace("[", "");
            output = output.replace("]", "");
            return output;
        } else
            return file;
    }
}
