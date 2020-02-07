--
-- PostgreSQL database dump
--

-- Dumped from database version 12.1
-- Dumped by pg_dump version 12.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: chats; Type: TABLE; Schema: public; Owner: bot_app
--

CREATE TABLE public.chats (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.chats OWNER TO bot_app;

--
-- Name: users; Type: TABLE; Schema: public; Owner: bot_app
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.users OWNER TO bot_app;

--
-- Name: users2chats; Type: TABLE; Schema: public; Owner: bot_app
--

CREATE TABLE public.users2chats (
    chat_id bigint NOT NULL,
    user_id bigint NOT NULL,
    active boolean DEFAULT false NOT NULL,
    rating integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.users2chats OWNER TO bot_app;

--
-- Data for Name: chats; Type: TABLE DATA; Schema: public; Owner: bot_app
--

COPY public.chats (id, name, active) FROM stdin;
-1001266898415	Очередной тест панин-бота	t
-1001287808434	Еще один тест папинбота	t
-322012842	Очередной тест панин-бота	t
-351812871	paninbottestDEV	t
926760034	Fuck	t
-376336636	Еще один тест папинбота	t
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: bot_app
--

COPY public.users (id, name) FROM stdin;
186959949	Jesus
58462501	Rishad Mustafaev
926760034	Rhett Butler [Back in business]
\.


--
-- Data for Name: users2chats; Type: TABLE DATA; Schema: public; Owner: bot_app
--

COPY public.users2chats (chat_id, user_id, active, rating) FROM stdin;
-376336636	58462501	t	0
-351812871	58462501	t	0
-376336636	926760034	t	0
926760034	926760034	f	0
-1001287808434	58462501	f	1
-1001287808434	926760034	f	2
-322012842	58462501	f	0
-1001266898415	58462501	t	0
-1001266898415	186959949	f	1
\.


--
-- Name: chats chats_pkey; Type: CONSTRAINT; Schema: public; Owner: bot_app
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pkey PRIMARY KEY (id);


--
-- Name: users2chats pk_users2chats; Type: CONSTRAINT; Schema: public; Owner: bot_app
--

ALTER TABLE ONLY public.users2chats
    ADD CONSTRAINT pk_users2chats PRIMARY KEY (chat_id, user_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: bot_app
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users2chats fk_users2chats_chat_id_id; Type: FK CONSTRAINT; Schema: public; Owner: bot_app
--

ALTER TABLE ONLY public.users2chats
    ADD CONSTRAINT fk_users2chats_chat_id_id FOREIGN KEY (chat_id) REFERENCES public.chats(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: users2chats fk_users2chats_user_id_id; Type: FK CONSTRAINT; Schema: public; Owner: bot_app
--

ALTER TABLE ONLY public.users2chats
    ADD CONSTRAINT fk_users2chats_user_id_id FOREIGN KEY (user_id) REFERENCES public.users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--

