package kasper.android.pulse.middleware;

import java.util.List;

import kasper.android.pulse.callbacks.middleware.OnComplexSyncListener;
import kasper.android.pulse.callbacks.middleware.OnComplexesSyncListener;
import kasper.android.pulse.callbacks.middleware.OnMeSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.retrofit.UserHandler;
import retrofit2.Call;

public class DataSyncer {

    public static void syncComplexWithServer(long complexId, final OnComplexSyncListener callback) {
        Packet complexPacket = new Packet();
        final Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        complexPacket.setComplex(complex);
        ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
        final Call<Packet> complexCall = complexHandler.getComplexById(complexPacket);
        NetworkHelper.requestServer(complexCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.Complex serverComplex = packet.getComplex();
                if (serverComplex != null) {
                    DatabaseHelper.notifyComplexCreated(serverComplex);
                    callback.complexSynced(serverComplex);
                }
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }

    public static void syncRoomWithServer(final long complexId, long roomId, final OnRoomSyncListener callback) {
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        final Entities.Room room = new Entities.Room();
        room.setRoomId(roomId);
        packet.setBaseRoom(room);
        RoomHandler roomHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
        Call<Packet> roomCall = roomHandler.getRoomById(packet);
        NetworkHelper.requestServer(roomCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.BaseRoom serverRoom = packet.getBaseRoom();
                DatabaseHelper.notifyRoomCreated(serverRoom);
                serverRoom = DatabaseHelper.getRoomById(roomId);
                callback.roomSynced(serverRoom);
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }

    public static void syncBaseUserWithServer(long userId, final OnBaseUserSyncListener callback) {
        Packet packet = new Packet();
        Entities.BaseUser user = new Entities.BaseUser();
        user.setBaseUserId(userId);
        packet.setBaseUser(user);
        UserHandler userHandler = NetworkHelper.getRetrofit().create(UserHandler.class);
        Call<Packet> userCall = userHandler.getUserById(packet);
        NetworkHelper.requestServer(userCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.BaseUser serverUser = packet.getBaseUser();
                if (serverUser != null) {
                    if (serverUser instanceof Entities.User) {
                        DatabaseHelper.notifyUserCreated((Entities.User) serverUser);
                    } else if (serverUser instanceof Entities.Bot) {
                        DatabaseHelper.notifyBotUpdated((Entities.Bot) serverUser);
                    }
                    callback.userSynced(serverUser);
                }
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }

    public static void syncMeWithServer(final OnMeSyncListener callback) {
        UserHandler userHandler = NetworkHelper.getRetrofit().create(UserHandler.class);
        Call<Packet> meCall = userHandler.getMe();
        NetworkHelper.requestServer(meCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.User serverMe = packet.getUser();
                Entities.UserSecret userSecret = packet.getUserSecret();
                DatabaseHelper.updateMe(serverMe);
                callback.meSynced(serverMe, userSecret.getHomeId());
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }

    public static void syncRoomsWithServer(final long complexId, final OnRoomsSyncListener callback) {
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        final RoomHandler roomHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
        Call<Packet> roomsCall = roomHandler.getRooms(packet);
        NetworkHelper.requestServer(roomsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                List<Entities.BaseRoom> rooms = packet.getBaseRooms();
                for (Entities.BaseRoom room : rooms) {
                    DatabaseHelper.notifyRoomCreated(room);
                }
                rooms = DatabaseHelper.getRooms(complexId);
                callback.roomsSynced(rooms);
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }

    public static void syncComplexesWithServer(final OnComplexesSyncListener callback) {
        ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
        Call<Packet> call = complexHandler.getComplexes();
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                List<Entities.Complex> complexes = packet.getComplexes();
                for (Entities.Complex complex : complexes) {
                    DatabaseHelper.notifyComplexCreated(complex);
                }
                complexes = DatabaseHelper.getComplexes();
                callback.complexesSynced(complexes);
            }

            @Override
            public void onServerFailure() {
                callback.syncFailed();
            }

            @Override
            public void onConnectionFailure() {
                callback.syncFailed();
            }
        });
    }
}
