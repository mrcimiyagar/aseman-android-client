package kasper.android.pulse.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.callbacks.ui.ComplexListener;
import kasper.android.pulse.callbacks.ui.ConnectionListener;
import kasper.android.pulse.callbacks.ui.ContactListener;
import kasper.android.pulse.callbacks.ui.DesktopListener;
import kasper.android.pulse.callbacks.ui.FileListener;
import kasper.android.pulse.callbacks.ui.MessageListener;
import kasper.android.pulse.callbacks.ui.ProfileListener;
import kasper.android.pulse.callbacks.ui.RoomListener;
import kasper.android.pulse.callbacks.ui.UiThreadListener;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

/**
 * Created by keyhan1376 on 3/9/2018.
 */

public class GraphicHelper {

    private static float density;
    public static float getDensity() {
        return density;
    }

    private static int screenWidth;
    public static int getScreenWidth() {
        return screenWidth;
    }

    private static int screenHeight;
    public static int getScreenHeight() {
        return screenHeight;
    }

    private static List<FileListener> fileListeners;
    public static List<FileListener> getFileListeners() {
        return fileListeners;
    }
    public static void addFileListener(FileListener fileListener) {
        GraphicHelper.fileListeners.add(fileListener);
    }
    public static FileListener getFileListener() {
        return new FileListener() {
            @Override
            public void fileUploaded(DocTypes docTypes, long localFileId, long onlineFileId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileUploaded(docTypes, localFileId, onlineFileId);
                        }
                    }
                });
            }

            @Override
            public void fileUploading(DocTypes docTypes, Entities.File file, Entities.FileLocal fileLocal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileUploading(docTypes, file, fileLocal);
                        }
                    }
                });
            }

            @Override
            public void fileUploadCancelled(DocTypes docTypes, long fileId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileUploadCancelled(docTypes, fileId);
                        }
                    }
                });
            }

            @Override
            public void fileDownloaded(DocTypes docTypes, long fileId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileDownloaded(docTypes, fileId);
                        }
                    }
                });
            }

            @Override
            public void fileDownloading(DocTypes docTypes, Entities.File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileDownloading(docTypes, file);
                        }
                    }
                });
            }

            @Override
            public void fileDownloadCancelled(DocTypes docTypes, long fileId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileDownloadCancelled(docTypes, fileId);
                        }
                    }
                });
            }

            @Override
            public void fileTransferProgressed(DocTypes docTypes, long fileId, int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileTransferProgressed(docTypes, fileId, progress);
                        }
                    }
                });
            }

            @Override
            public void fileReceived(DocTypes docTypes, Entities.File file, Entities.FileLocal fileLocal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (FileListener fileListener : fileListeners) {
                            fileListener.fileReceived(docTypes, file, fileLocal);
                        }
                    }
                });
            }
        };
    }

    private static List<MessageListener> messageListeners;
    public static List<MessageListener> getMessageListeners() {
        return messageListeners;
    }
    public static void addMessageListener(MessageListener messageListener) {
        GraphicHelper.messageListeners.add(messageListener);
    }
    public static MessageListener getMessageListener() {
        return new MessageListener() {
            @Override
            public void messageReceived(Entities.Message message, Entities.MessageLocal messageLocal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (MessageListener messageListener : messageListeners) {
                            messageListener.messageReceived(message, messageLocal);
                        }
                    }
                });
            }

            @Override
            public void messageDeleted(Entities.Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (MessageListener messageListener : messageListeners) {
                            messageListener.messageDeleted(message);
                        }
                    }
                });
            }

            @Override
            public void messageSending(Entities.Message message, Entities.MessageLocal messageLocal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (MessageListener messageListener : messageListeners) {
                            messageListener.messageSending(message, messageLocal);
                        }
                    }
                });
            }

            @Override
            public void messageSent(long localMessageId, long onlineMessageId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (MessageListener messageListener : messageListeners) {
                            messageListener.messageSent(localMessageId, onlineMessageId);
                        }
                    }
                });
            }
        };
    }

    private static List<ComplexListener> complexListeners;
    public static List<ComplexListener> getComplexListeners() {
        return complexListeners;
    }
    public static void addComplexListener(ComplexListener complexListener) {
        GraphicHelper.complexListeners.add(complexListener);
    }
    public static ComplexListener getComplexListener() {
        return new ComplexListener() {
            @Override
            public void notifyComplexCreated(Entities.Complex complex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ComplexListener complexListener : complexListeners) {
                            complexListener.notifyComplexCreated(complex);
                        }
                    }
                });
            }

            @Override
            public void notifyComplexCreated(Entities.Contact contact, Entities.Complex complex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ComplexListener complexListener : complexListeners) {
                            complexListener.notifyComplexCreated(complex);
                        }
                    }
                });
            }

            @Override
            public void notifyComplexRemoved(long complexId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ComplexListener complexListener : complexListeners) {
                            complexListener.notifyComplexRemoved(complexId);
                        }
                    }
                });
            }

            @Override
            public void notifyComplexesCreated(List<Entities.Complex> complexes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ComplexListener complexListener : complexListeners) {
                            complexListener.notifyComplexesCreated(complexes);
                        }
                    }
                });
            }
        };
    }

    private static List<RoomListener> roomListeners;
    public static List<RoomListener> getRoomListeners() {
        return roomListeners;
    }
    public static void addRoomListener(RoomListener roomListener) {
        GraphicHelper.roomListeners.add(roomListener);
    }
    public static RoomListener getRoomListener() {
        return new RoomListener() {
            @Override
            public void roomCreated(long complexId, Entities.Room room) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (RoomListener roomListener : roomListeners) {
                            roomListener.roomCreated(complexId, room);
                        }
                    }
                });
            }

            @Override
            public void roomsCreated(long complexId, List<Entities.Room> rooms) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (RoomListener roomListener : roomListeners) {
                            roomListener.roomsCreated(complexId, rooms);
                        }
                    }
                });
            }

            @Override
            public void roomRemoved(Entities.Room room) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (RoomListener roomListener : roomListeners) {
                            roomListener.roomRemoved(room);
                        }
                    }
                });
            }

            @Override
            public void updateRoomLastMessage(long roomId, Entities.Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (RoomListener roomListener : roomListeners) {
                            roomListener.updateRoomLastMessage(roomId, message);
                        }
                    }
                });
            }
        };
    }

    private static List<ContactListener> contactListeners;
    public static List<ContactListener> getContactListeners() {
        return contactListeners;
    }
    public static void addContactListener(ContactListener contactListener) {
        GraphicHelper.contactListeners.add(contactListener);
    }
    public static ContactListener getContactListener() {
        return new ContactListener() {
            @Override
            public void contactCreated(Entities.Contact contact) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ContactListener contactListener : contactListeners) {
                            contactListener.contactCreated(contact);
                        }
                    }
                });
            }
        };
    }

    private static UiThreadListener uiThreadListener;
    public static void setUiThreadListener(UiThreadListener uiThreadListener) {
        GraphicHelper.uiThreadListener = uiThreadListener;
    }
    public static void runOnUiThread(Runnable runnable) {
        try {
            GraphicHelper.uiThreadListener.runOnUiThread(runnable);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static List<ConnectionListener> connectionListeners;
    public static List<ConnectionListener> getConnectionListeners() {
        return connectionListeners;
    }
    public static void addConnectionListener(ConnectionListener connectionListener) {
        GraphicHelper.connectionListeners.add(connectionListener);
    }
    public static ConnectionListener getConnectionListener() {
        return new ConnectionListener() {
            @Override
            public void reconnecting() {
                runOnUiThread(() -> {
                    for (ConnectionListener connectionListener : connectionListeners) {
                        connectionListener.reconnecting();
                    }
                });
            }
            @Override
            public void connected() {
                runOnUiThread(() -> {
                    for (ConnectionListener connectionListener : connectionListeners) {
                        connectionListener.connected();
                    }
                });
            }
        };
    }

    private static List<ProfileListener> profileListeners;
    public static List<ProfileListener> getProfileListeners() {
        return profileListeners;
    }
    public static void addProfileListener(ProfileListener profileListener) {
        GraphicHelper.profileListeners.add(profileListener);
    }
    public static ProfileListener getProfileListener() {
        return new ProfileListener() {
            @Override
            public void profileUpdated(Entities.User user) {
                GraphicHelper.runOnUiThread(() -> {
                    for (ProfileListener profileListener : profileListeners) {
                        profileListener.profileUpdated(user);
                    }
                });
            }

            @Override
            public void profileUpdated(Entities.Complex complex) {
                GraphicHelper.runOnUiThread(() -> {
                    for (ProfileListener profileListener : profileListeners) {
                        profileListener.profileUpdated(complex);
                    }
                });
            }

            @Override
            public void profileUpdated(Entities.Room room) {
                GraphicHelper.runOnUiThread(() -> {
                    for (ProfileListener profileListener : profileListeners) {
                        profileListener.profileUpdated(room);
                    }
                });
            }

            @Override
            public void profileUpdated(Entities.Bot bot) {
                GraphicHelper.runOnUiThread(() -> {
                    for (ProfileListener profileListener : profileListeners) {
                        profileListener.profileUpdated(bot);
                    }
                });
            }
        };
    }

    private static List<DesktopListener> desktopListeners;
    public static List<DesktopListener> getDesktopListeners() {
        return desktopListeners;
    }
    public static void addDesktopListener(DesktopListener desktopListener) {
        GraphicHelper.desktopListeners.add(desktopListener);
    }
    public static DesktopListener getDesktopListener() {
        return new DesktopListener() {
            @Override
            public void workerAdded(Entities.Workership workership) {
                runOnUiThread(() -> {
                    for (DesktopListener desktopListener : desktopListeners) {
                        desktopListener.workerAdded(workership);
                    }
                });
            }

            @Override
            public void workerUpdated(Entities.Workership workership) {
                runOnUiThread(() -> {
                    for (DesktopListener desktopListener : desktopListeners) {
                        desktopListener.workerUpdated(workership);
                    }
                });
            }

            @Override
            public void workerRemoved(Entities.Workership workership) {
                runOnUiThread(() -> {
                    for (DesktopListener desktopListener : desktopListeners) {
                        desktopListener.workerRemoved(workership);
                    }
                });
            }
        };
    }

    public static void setup(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        fileListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
        complexListeners = new ArrayList<>();
        roomListeners = new ArrayList<>();
        contactListeners = new ArrayList<>();
        connectionListeners = new ArrayList<>();
        profileListeners = new ArrayList<>();
        desktopListeners = new ArrayList<>();
    }

    public static int dpToPx(float dp) {
        return (int)(dp * density);
    }

    public static int pxToDp(float px) {
        return (int)(px / density);
    }
}
