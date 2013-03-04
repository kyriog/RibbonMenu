package com.darvds.ribbonmenu;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RibbonMenuView extends LinearLayout {
	private ListView rbmListView;
	private View rbmOutsideView;
	private iRibbonMenuCallback callback;
	private OnRibbonChangeListener listener;
	private int activePosition = -1;
	
	private static ArrayList<RibbonMenuItem> menuItems;
	
	public RibbonMenuView(Context context) {
		super(context);	
		load();
	}
	
	public RibbonMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		load();
	}
	
	private void load() {
		if(isInEditMode())
			return;
		inflateLayout();
		initUi();
	}
	
	private void inflateLayout() {
		try {
			LayoutInflater.from(getContext()).inflate(R.layout.rbm_menu, this, true);
		} catch(Exception e){}
	}
	
	private void initUi(){
		rbmListView = (ListView) findViewById(R.id.rbm_listview);
		rbmOutsideView = (View) findViewById(R.id.rbm_outside_view);
		
		rbmOutsideView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideMenu();
			}
		});
		
		rbmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				setActivePosition(position);
				
				if(callback != null)
					callback.RibbonMenuItemClick(menuItems.get(position).id);
				
				hideMenu();
			}
		});
	}
	
	private void setActivePosition(int position) {
		if(activePosition != -1)
			rbmListView.getChildAt(activePosition).setBackgroundResource(R.color.rbm_menu_background);
		rbmListView.getChildAt(position).setBackgroundResource(R.color.rbm_active_item_background);
		activePosition = position;
	}
	
	public void setActiveItem(int itemId) {
		int size = menuItems.size();
		for(int i = 0; i < size; i++) {
			if(menuItems.get(i).id == itemId) {
				setActivePosition(i);
				break;
			}
		}
	}
	
	public void setListener(OnRibbonChangeListener listener) {
		this.listener = listener;
	}
	
	public void setMenuClickCallback(iRibbonMenuCallback callback) {
		this.callback = callback;
	}
	
	public void setMenuItems(int menu) {
		parseXml(menu);
		
		if(menuItems != null && menuItems.size() > 0)
			rbmListView.setAdapter(new Adapter());
	}
	
	public void setBackgroundResource(int resource) {
		rbmListView.setBackgroundResource(resource);
	}
	
	public void showMenu() {
		rbmOutsideView.setVisibility(View.VISIBLE);
		
		rbmListView.setVisibility(View.VISIBLE);
		rbmListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rbm_in_from_left));
		
		if(listener != null)
			listener.onRibbonOpen();
	}
	
	public void hideMenu() {
		rbmOutsideView.setVisibility(View.GONE);
		rbmListView.setVisibility(View.GONE);
		
		rbmListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rbm_out_to_left));
		
		if(listener != null)
			listener.onRibbonClose();
	}
	
	public void toggleMenu() {
		if(rbmOutsideView.getVisibility() == View.GONE)
			showMenu();
		else
			hideMenu();
	}
	
	public boolean isOpen() {
		return rbmOutsideView.getVisibility() == View.VISIBLE;
	}
	
	private void parseXml(int menu) {
		menuItems = new ArrayList<RibbonMenuView.RibbonMenuItem>();
		
		try {
			XmlResourceParser xpp = getResources().getXml(menu);
			
			xpp.next();
			int eventType = xpp.getEventType();
			
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					String elemName = xpp.getName();
					
					if(elemName.equals("item")) {
						String textId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "title");
						String iconId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "icon");
						String resId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "id");
						
						RibbonMenuItem item = new RibbonMenuItem();
						item.id = Integer.valueOf(resId.replace("@", ""));
						item.text = resourceIdToString(textId);
						item.icon = Integer.valueOf(iconId.replace("@", ""));
						
						menuItems.add(item);
					}
				}
				
				eventType = xpp.next();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String resourceIdToString(String text) {
		if(!text.contains("@"))
			return text;
		else {
			String id = text.replace("@", "");
			return getResources().getString(Integer.valueOf(id));
		}
	}
	
	public boolean isMenuVisible() {
		return rbmOutsideView.getVisibility() == View.VISIBLE;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)	{
	    SavedState ss = (SavedState)state;
	    super.onRestoreInstanceState(ss.getSuperState());
	    
	    activePosition = ss.activePosition;
	    
	    if (ss.bShowMenu)
	        showMenu();
	    else
	        hideMenu();
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
	    Parcelable superState = super.onSaveInstanceState();
	    SavedState ss = new SavedState(superState);

	    ss.bShowMenu = isMenuVisible();
	    ss.activePosition = activePosition;

	    return ss;
	}
	
	static class SavedState extends BaseSavedState {
	    boolean bShowMenu;
	    int activePosition;

	    SavedState(Parcelable superState) {
	        super(superState);
	    }
	    
	    private SavedState(Parcel in) {
	        super(in);
	        int[] saved = new int[2];
	        in.readIntArray(saved);
	        bShowMenu = (saved[0] == 1);
	        activePosition = saved[1];
	    }
	    
	    @Override
	    public void writeToParcel(Parcel out, int flags) {
	        super.writeToParcel(out, flags);
	        int[] write = new int[2];
	        write[0] = (bShowMenu ? 1 : 0);
	        write[1] = activePosition;
	        out.writeIntArray(write);
	    }
	    
	    public static final Parcelable.Creator<SavedState> CREATOR
	            = new Parcelable.Creator<SavedState>() {
	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }
	        
	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}
	
	class RibbonMenuItem {
		int id;
		String text;
		int icon;
	}
	
	private class Adapter extends BaseAdapter {
		private LayoutInflater inflater;
		
		public Adapter() {
			inflater = LayoutInflater.from(getContext());
		}
		
		@Override
		public int getCount() {
			return menuItems.size();
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			
			if(convertView == null || convertView instanceof TextView) {
				convertView = inflater.inflate(R.layout.rbm_item, null);
				
				if(position == activePosition)
					convertView.setBackgroundResource(R.color.rbm_active_item_background);
				
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.rbm_item_icon);
				holder.text = (TextView) convertView.findViewById(R.id.rbm_item_text);
				
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			
			holder.image.setImageResource(menuItems.get(position).icon);
			holder.text.setText(menuItems.get(position).text);
			
			return convertView;
		}
		
		class ViewHolder {
			TextView text;
			ImageView image;
		}
	}
}
