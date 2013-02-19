package org.softeg.slartus.forpda.topicview;

import android.app.AlertDialog;
import android.app.Service;
import android.content.*;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.QuickStartActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.ForumTreeTab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.ProfileMenuFragment;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.HelpTask;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.search.SearchActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 01.11.12
 * Time: 7:18
 * To change this template use File | Settings | File Templates.
 */
public final class TopicViewMenuFragment extends ProfileMenuFragment {

    private ThemeActivity getInterface() {
        if (getActivity() == null) return null;
        return (ThemeActivity) getActivity();
    }

    public TopicViewMenuFragment() {
        super();

    }


    private Boolean m_FirstTime = true;

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        if (!m_FirstTime)
            getInterface().onPrepareOptionsMenu();
        m_FirstTime = false;
        if (mTopicOptionsMenu != null)
            configureOptionsMenu(getActivity(), getInterface().getHandler(), mTopicOptionsMenu, getInterface().getTopic(),
                    true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
        else if (getInterface() != null && getInterface().getTopic() != null)
            mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface().getTopic(),
                    true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
    }

    private com.actionbarsherlock.view.SubMenu mTopicOptionsMenu;

    private static com.actionbarsherlock.view.SubMenu addOptionsMenu(final Context context, final Handler mHandler, com.actionbarsherlock.view.Menu menu, final org.softeg.slartus.forpda.classes.Topic topic,
                                                                     Boolean addFavorites, final String shareItUrl) {
        com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Опции");
        optionsMenu.getItem().setIcon(android.R.drawable.ic_menu_more);
        configureOptionsMenu(context, mHandler, optionsMenu, topic, addFavorites, shareItUrl);
        return optionsMenu;
    }

    private static void configureOptionsMenu(final Context context, final Handler mHandler, com.actionbarsherlock.view.SubMenu optionsMenu, final org.softeg.slartus.forpda.classes.Topic topic,
                                             Boolean addFavorites, final String shareItUrl) {

        optionsMenu.clear();

        if (Client.INSTANCE.getLogined() && topic != null) {
            if (addFavorites) {
                optionsMenu.add("Добавить в избранное").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            final HelpTask helpTask = new HelpTask(context, "Добавление в избранное");
                            helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                                public Object onMethod(Object param) {
                                    if (helpTask.Success)
                                        Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                    else
                                        Log.e(context, helpTask.ex);
                                    return null;
                                }
                            });
                            helpTask.execute(new HelpTask.OnMethodListener() {
                                public Object onMethod(Object param) throws IOException {
                                    return topic.addToFavorites();
                                }
                            }
                            );
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }

                        return true;
                    }
                });

                optionsMenu.add("Удалить из избранного").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            final HelpTask helpTask = new HelpTask(context, "Удаление из избранного");
                            helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                                public Object onMethod(Object param) {
                                    if (helpTask.Success)
                                        Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                    else
                                        Log.e(context, helpTask.ex);
                                    return null;
                                }
                            });
                            helpTask.execute(new HelpTask.OnMethodListener() {
                                public Object onMethod(Object param) throws IOException {
                                    return topic.removeFromFavorites();  //To change body of implemented methods use File | Settings | File Templates.
                                }
                            }
                            );
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }
                        return true;
                    }
                });

                optionsMenu.add("Подписаться").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            topic.startSubscribe(context, mHandler);
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }


                        return true;
                    }
                });

                optionsMenu.add("Отписаться").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            topic.unSubscribe(context, mHandler);
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }
                        return true;
                    }
                });

                optionsMenu.add("Открыть форум темы").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            Intent intent = new Intent(context, QuickStartActivity.class);

                            intent.putExtra("template", Tabs.TAB_FORUMS);
                            intent.putExtra(ForumTreeTab.KEY_FORUM_ID, topic.getForumId());
                            intent.putExtra(ForumTreeTab.KEY_FORUM_TITLE, topic.getForumTitle());
                            intent.putExtra(ForumTreeTab.KEY_TOPIC_ID, topic.getId());
                            context.startActivity(intent);
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }
                        return true;
                    }
                });
            }
        }

        optionsMenu.add("Поделиться ссылкой").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                try {
                    String url = TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + topic.getId()) : shareItUrl;
                    ExtUrl.shareIt(context, url);
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
    }

    private com.actionbarsherlock.view.MenuItem m_EditPost;
    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, final com.actionbarsherlock.view.MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {

            com.actionbarsherlock.view.MenuItem item = menu.add("Вложения")
                    .setIcon(R.drawable.ic_menu_download)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().showTopicAttaches();

                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            SubMenu subMenu = menu.addSubMenu("Найти на странице").setIcon(android.R.drawable.ic_menu_search);
            subMenu.getItem().setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            subMenu.add("Найти на странице")
                    .setIcon(android.R.drawable.ic_menu_search)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().onSearchRequested();

                            return true;
                        }
                    });
            subMenu.add("Найти в этой теме")
                    .setIcon(android.R.drawable.ic_menu_search)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            try {
                                View searchView = LayoutInflater.from(getActivity()).inflate(R.layout.search_input, null);
                                final EditText editText = (EditText) searchView.findViewById(R.id.query_edit);
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                        .setTitle("Найти в этой теме")
                                        .setView(searchView)
                                        .setPositiveButton("Поиск", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                SearchActivity.startActivity(getActivity(),
                                                        getInterface().getTopic().getForumId(),
                                                        getInterface().getTopic().getForumTitle(),
                                                        getInterface().getTopic().getId(),
                                                        editText.getText().toString());
                                            }
                                        })
                                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    public void onShow(DialogInterface dialogInterface) {
                                        editText.requestFocus();
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(editText, 0);
                                    }
                                });
                                alertDialog.show();


                            } catch (Exception ex) {
                                Log.e(getInterface(), ex);
                            }


                            return true;
                        }
                    });

            m_EditPost = menu.add("Написать")
                    .setIcon(android.R.drawable.ic_menu_edit)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().toggleMessagePanelVisibility();

                            return true;
                        }
                    });
            m_EditPost.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


            menu.add("Обновить")
                    .setIcon(R.drawable.ic_menu_refresh)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().rememberScrollX();
//                            m_ScrollY = webView.getScrollY();
//                            m_ScrollX = webView.getScrollX();
                            getInterface().showTheme(getInterface().getLastUrl());


                            return true;
                        }
                    });
            menu.add("Браузер")
                    .setIcon(R.drawable.ic_menu_goto)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            try {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://" + Client.SITE + "/forum/index.php?" + getInterface().getLastUrl()));
                                startActivity(Intent.createChooser(marketIntent, "Выберите"));


                            } catch (ActivityNotFoundException e) {
                                Log.e(getActivity(), e);
                            }


                            return true;
                        }
                    });
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            if (getInterface() != null)
                mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface().getTopic(),
                        true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());

            com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Настройки");
            optionsMenu.getItem().setIcon(android.R.drawable.ic_menu_preferences);
            optionsMenu.getItem().setTitle("Настройки");
            optionsMenu.add("Масштабировать").setIcon(android.R.drawable.ic_menu_preferences)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                prefs.getBoolean("theme.ZoomUsing", true);
                                menuItem.setChecked(!menuItem.isChecked());
                                getInterface().setAndSaveUseZoom(menuItem.isChecked());

                            } catch (Exception ex) {
                                Log.e(getActivity(), ex);
                            }


                            return true;
                        }
                    }).setCheckable(true).setChecked(prefs.getBoolean("theme.ZoomUsing", true));


            optionsMenu.add("Запомнить масштаб").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    try {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("theme.ZoomLevel", Integer.toString((int) (getInterface().getWebView().getScale() * 100)));
                        editor.commit();
                        getInterface().getWebView().setInitialScale((int) (getInterface().getWebView().getScale() * 100));
                        Toast.makeText(getActivity(), "Масштаб запомнен", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Log.e(getActivity(), ex);
                    }


                    return true;
                }
            });


            optionsMenu.add("Загружать изображения").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Boolean loadImagesAutomatically1 = getInterface().getWebViewExternals().getLoadsImagesAutomatically();
                    getInterface().getWebViewExternals().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    menuItem.setChecked(!loadImagesAutomatically1);
                    return true;
                }
            }).setCheckable(true).setChecked(getInterface().getWebView().getSettings().getLoadsImagesAutomatically());

            optionsMenu.add("Стиль").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    showStylesDialog(prefs);
                    return true;
                }
            });

            item =menu.add("Закрыть")
                    .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().getPostBody();
                            if (!TextUtils.isEmpty(getInterface().getPostBody())) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Подтвердите действие")
                                        .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                getInterface().clear();
                                                getInterface().finish();
                                            }
                                        })
                                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();
                            } else {
                                getInterface().clear(true);
                                getInterface().finish();
                            }

                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } catch (Exception ex) {
            Log.e(getActivity(), ex);
        }


    }

    private void showStylesDialog(final SharedPreferences prefs) {
        try {
            final String currentValue = MyApp.INSTANCE.getCurrentTheme();
            String[] styleNames = getInterface().getResources().getStringArray(R.array.appthemesArray);
            String[] styleValues = getInterface().getResources().getStringArray(R.array.appthemesValues);
            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
            final ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();
            for (CharSequence s : styleNames) {
                newStyleNames.add(s);
            }

            for (CharSequence s : styleValues) {
                newstyleValues.add(s);
            }
            File file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/");
            if (file.exists()) {
                file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/white/");
                if (file.exists()) {
                    File[] cssFiles = file.listFiles(new FilenameFilter() {
                        public boolean accept(File file, String s) {
                            return s.endsWith(".css");
                        }
                    });
                    for (File cssFile : cssFiles) {
                        newStyleNames.add(cssFile.getName());
                        newstyleValues.add(cssFile.getPath());
                    }
                }

                file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/black/");
                if (file.exists()) {
                    File[] cssFiles = file.listFiles(new FilenameFilter() {
                        public boolean accept(File file, String s) {
                            return s.endsWith(".css");
                        }
                    });
                    for (File cssFile : cssFiles) {
                        newStyleNames.add(cssFile.getName());
                        newstyleValues.add(cssFile.getPath());
                    }
                }
            }
            final int[] selected = {newstyleValues.indexOf(currentValue)};
            new AlertDialog.Builder(getActivity())
                    .setTitle("Стиль")
                    .setCancelable(false)
                    .setSingleChoiceItems(newStyleNames.toArray(new CharSequence[newStyleNames.size()]),newstyleValues.indexOf(currentValue),new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selected[0] =i;
                        }
                    })
                    .setPositiveButton("Применить и перезагрузить страницу",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(selected[0]==-1){
                                Toast.makeText(getActivity(),"Выберите стиль",Toast.LENGTH_LONG).show();
                                return ;
                            }
                            dialogInterface.dismiss();
                            prefs.edit().putString("appstyle",newstyleValues.get(selected[0]).toString()).commit();
                            getInterface().rememberScrollX();
//                            m_ScrollY = webView.getScrollY();
//                            m_ScrollX = webView.getScrollX();
                            getInterface().showTheme(getInterface().getLastUrl());
                        }
                    })
                    .setNegativeButton("Отмена",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } catch (Exception ex) {
            Log.e(getInterface(), ex);
        }
    }
}
