package com.application.pm1_proyecto_final.api;

public class GroupApiMethods {


    public static final String URL = "https://dennisdomain.com/";
    public static final String URL_LOCAL = "https://localhost:8000/";


    public static final String POST_GROUP = URL + "api/groups";


    public static final String POST_GROUP_USER = URL + "api/groupUsers";

    public static final String GET_GROUP_USER_CREATE = URL + "api/usersGroupCreates/";

    public static final String GET_USERS_FOR_GROUP = URL + "api/usuariosDelGrupo/";


    public static final String GET_USERS_FOR_GROUP_ACTIVE = URL + "api/usuariosDelGrupoActivos/";




    //Metodos de la relacion muchos a muchos entre grupos y usuarios

    public static final String POST_STATUS_USER_GROUP = URL + "api/getStatusUserGroup/";

    public static final String POST_USER_GROUP = URL + "api/groupUsers/";

    public static final String POST_USER_GROUP_UPDATE = URL + "api/updateUserGroup/";


    public static final String GET_USER_INVITATION = URL + "api/invitacionesPorUsuario/";

    public static final String GET_GROUPS_FOR_USER_ACTIVE = URL + "api/gruposDelUsuario/";




}
