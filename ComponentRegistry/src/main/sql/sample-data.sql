--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.6
-- Dumped by pg_dump version 9.6.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = public, pg_catalog;

--
-- Data for Name: registry_user; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY registry_user (id, name, principal_name) FROM stdin;
1	The Administrator	admin
\.


--
-- Data for Name: basedescription; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY basedescription (id, user_id, is_public, is_deleted, component_id, name, description, registration_date, creator_name, domain_name, group_name, href, show_in_editor, content, status, derivedfrom, successor) FROM stdin;
3	1	t	f	clarin.eu:cr1:p_1512739327528	ExampleProfile	Example profile referencing some components	2017-12-08 13:27:26.017+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="true" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:p_1512739327528</ID>\n        <Name>ExampleProfile</Name>\n        <Description>Example profile referencing some components</Description>\n        <Status>production</Status>\n    </Header>\n    <Component name="ExampleProfile" ConceptLink="http://test-concepts.org/exampleProfile" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="1" CardinalityMax="1" Multilingual="true" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>First element at profile root level</Documentation>\n        </Element>\n        <Component name="Children" ConceptLink="http://test-concepts.org/children" CardinalityMin="1" CardinalityMax="1">\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526"/>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327527" CardinalityMin="0"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	1	\N	\N
1	1	t	f	clarin.eu:cr1:c_1512739327526	StandaloneSampleComponent	For testing purposes. Component that does not reference other components	2017-12-08 13:27:08.822+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512739327526</ID>\n        <Name>StandaloneSampleComponent</Name>\n        <Description>For testing purposes. Component that does not reference other components</Description>\n        <Status>production</Status>\n    </Header>\n    <Component name="StandaloneSampleComponent" ConceptLink="http://test-concepts.org/standAloneSampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="1" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>Test element</Documentation>\n        </Element>\n        <Element name="SecondElement" ConceptLink="http://test-concepts.org/secondElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="unbounded">\n            <Documentation>Another test element, with an attribute</Documentation>\n            <AttributeList>\n                <Attribute name="SecondElementAttribute" ConceptLink="http://test-concepts.org/someAttribute" ValueScheme="int">\n                    <Documentation>An optional attribute on an element</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Element>\n        <Component name="FirstChildComponent" ConceptLink="http://test-concepts.org/firstComponent" CardinalityMin="1" CardinalityMax="unbounded">\n            <Documentation>First child component</Documentation>\n            <AttributeList>\n                <Attribute name="FirstChildComponentAttribute" ConceptLink="http://test-concepts.org/componentAttribute" ValueScheme="string" Required="true">\n                    <Documentation>Mandatory attribute on a component</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Component>\n    </Component>\n</ComponentSpec>\n	1	\N	\N
2	1	t	f	clarin.eu:cr1:c_1512739327527	WrappingExampleComponent	Component referencing another component	2017-12-08 13:27:16.803+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512739327527</ID>\n        <Name>WrappingExampleComponent</Name>\n        <Description>Component referencing another component</Description>\n        <Status>production</Status>\n    </Header>\n    <Component name="WrappingExampleComponent" ConceptLink="http://test-concepts.org/wrappingExampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Component ComponentRef="clarin.eu:cr1:c_1512739327526" CardinalityMin="0" CardinalityMax="unbounded"/>\n        <Component name="ChildComponent" ConceptLink="http://test-concepts.org/childComponent" CardinalityMin="1" CardinalityMax="1">\n            <Documentation>Inline child component referencing another component</Documentation>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526" CardinalityMin="0"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	1	\N	\N
6	1	f	f	clarin.eu:cr1:p_1512740341364	MyExampleProfile	Example profile referencing some components	2017-12-08 13:40:39.275+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="true" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:p_1512740341364</ID>\n        <Name>MyExampleProfile</Name>\n        <Description>Example profile referencing some components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:p_1512739327528</DerivedFrom>\n    </Header>\n    <Component name="MyExampleProfile" ConceptLink="http://test-concepts.org/exampleProfile" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="1" CardinalityMax="1" Multilingual="true" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>First element at profile root level</Documentation>\n        </Element>\n        <Component name="Children" ConceptLink="http://test-concepts.org/children" CardinalityMin="1" CardinalityMax="1">\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526"/>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327527" CardinalityMin="0"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:p_1512739327528	\N
8	1	t	f	clarin.eu:cr1:p_1512740341366	DraftExampleProfile	Example profile referencing some components	2017-12-08 13:46:50.651+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="true" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:p_1512740341366</ID>\n        <Name>DraftExampleProfile</Name>\n        <Description>Example profile referencing some components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:p_1512740341364</DerivedFrom>\n    </Header>\n    <Component name="DraftExampleProfile" ConceptLink="http://test-concepts.org/exampleProfile" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="1" CardinalityMax="1" Multilingual="true" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>First element at profile root level</Documentation>\n        </Element>\n        <Component name="Children" ConceptLink="http://test-concepts.org/children" CardinalityMin="1" CardinalityMax="1">\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526"/>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327527" CardinalityMin="0"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:p_1512740341364	\N
7	1	t	f	clarin.eu:cr1:c_1512740341365	DraftStandaloneComponent	For testing purposes. Draft component that does not reference other components	2017-12-08 13:46:30.536+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512740341365</ID>\n        <Name>DraftStandaloneComponent</Name>\n        <Description>For testing purposes. Draft component that does not reference other components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:c_1512740341362</DerivedFrom>\n    </Header>\n    <Component name="DraftStandaloneComponent" ConceptLink="http://test-concepts.org/standAloneSampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="1" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>Test element</Documentation>\n        </Element>\n        <Element name="SecondElement" ConceptLink="http://test-concepts.org/secondElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="unbounded">\n            <Documentation>Another test element, with an attribute</Documentation>\n            <AttributeList>\n                <Attribute name="SecondElementAttribute" ConceptLink="http://test-concepts.org/someAttribute" ValueScheme="int">\n                    <Documentation>An optional attribute on an element</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Element>\n        <Component name="FirstChildComponent" ConceptLink="http://test-concepts.org/firstComponent" CardinalityMin="1" CardinalityMax="unbounded">\n            <Documentation>First child component</Documentation>\n            <AttributeList>\n                <Attribute name="FirstChildComponentAttribute" ConceptLink="http://test-concepts.org/componentAttribute" ValueScheme="string" Required="true">\n                    <Documentation>Mandatory attribute on a component</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:c_1512740341362	\N
4	1	f	f	clarin.eu:cr1:c_1512740341362	MyStandaloneComponent	For testing purposes. Private component that does not reference other components	2017-12-08 13:47:31.52+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512740341362</ID>\n        <Name>MyStandaloneComponent</Name>\n        <Description>For testing purposes. Private component that does not reference other components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:c_1512739327526</DerivedFrom>\n    </Header>\n    <Component name="MyStandaloneComponent" ConceptLink="http://test-concepts.org/standAloneSampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="1" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>Test element</Documentation>\n        </Element>\n        <Element name="SecondElement" ConceptLink="http://test-concepts.org/secondElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="unbounded">\n            <Documentation>Another test element, with an attribute</Documentation>\n            <AttributeList>\n                <Attribute name="SecondElementAttribute" ConceptLink="http://test-concepts.org/someAttribute" ValueScheme="int">\n                    <Documentation>An optional attribute on an element</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Element>\n        <Component name="FirstChildComponent" ConceptLink="http://test-concepts.org/firstComponent" CardinalityMin="1" CardinalityMax="unbounded">\n            <Documentation>First child component</Documentation>\n            <AttributeList>\n                <Attribute name="FirstChildComponentAttribute" ConceptLink="http://test-concepts.org/componentAttribute" ValueScheme="string" Required="true">\n                    <Documentation>Mandatory attribute on a component</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:c_1512739327526	\N
5	1	f	f	clarin.eu:cr1:c_1512740341363	MyWrappingExampleComponent	Private component referencing another component	2017-12-08 13:47:43.273+00	admin		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512740341363</ID>\n        <Name>MyWrappingExampleComponent</Name>\n        <Description>Private component referencing another component</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:c_1512739327527</DerivedFrom>\n    </Header>\n    <Component name="MyWrappingExampleComponent" ConceptLink="http://test-concepts.org/wrappingExampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Component ComponentRef="clarin.eu:cr1:c_1512740341362"/>\n        <Component name="ChildComponent" ConceptLink="http://test-concepts.org/childComponent" CardinalityMin="1" CardinalityMax="1">\n            <Documentation>Inline child component referencing another component</Documentation>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526" CardinalityMin="0"/>\n            <Component ComponentRef="clarin.eu:cr1:c_1512740341365"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:c_1512739327527	\N
9	1	f	f	clarin.eu:cr1:p_1512742972468	TeamExampleProfile	Team profile referencing some components	2017-12-08 14:22:52.469+00	The Administrator		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="true" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:p_1512742972468</ID>\n        <Name>TeamExampleProfile</Name>\n        <Description>Team profile referencing some components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:p_1512740341364</DerivedFrom>\n    </Header>\n    <Component name="TeamExampleProfile" ConceptLink="http://test-concepts.org/exampleProfile" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="1" CardinalityMax="1" Multilingual="true" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>First element at profile root level</Documentation>\n        </Element>\n        <Component name="Children" ConceptLink="http://test-concepts.org/children" CardinalityMin="1" CardinalityMax="1">\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327526"/>\n            <Component ComponentRef="clarin.eu:cr1:c_1512739327527" CardinalityMin="0"/>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:p_1512740341364	\N
10	1	f	f	clarin.eu:cr1:c_1512742972469	TeamStandaloneComponent	For testing purposes. Private component that does not reference other components	2017-12-08 14:23:13.732+00	The Administrator		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512742972469</ID>\n        <Name>TeamStandaloneComponent</Name>\n        <Description>For testing purposes. Private component that does not reference other components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:c_1512740341362</DerivedFrom>\n    </Header>\n    <Component name="TeamStandaloneComponent" ConceptLink="http://test-concepts.org/standAloneSampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="1" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>Test element</Documentation>\n        </Element>\n        <Element name="SecondElement" ConceptLink="http://test-concepts.org/secondElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="unbounded">\n            <Documentation>Another test element, with an attribute</Documentation>\n            <AttributeList>\n                <Attribute name="SecondElementAttribute" ConceptLink="http://test-concepts.org/someAttribute" ValueScheme="int">\n                    <Documentation>An optional attribute on an element</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Element>\n        <Component name="FirstChildComponent" ConceptLink="http://test-concepts.org/firstComponent" CardinalityMin="1" CardinalityMax="unbounded">\n            <Documentation>First child component</Documentation>\n            <AttributeList>\n                <Attribute name="FirstChildComponentAttribute" ConceptLink="http://test-concepts.org/componentAttribute" ValueScheme="string" Required="true">\n                    <Documentation>Mandatory attribute on a component</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:c_1512740341362	\N
11	1	f	t	clarin.eu:cr1:c_1512742972470	DeletedComponent	For testing purposes. Private component that does not reference other components	2017-12-08 14:24:24.781+00	The Administrator		example	\N	t	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<ComponentSpec isProfile="false" CMDVersion="1.2" CMDOriginalVersion="1.2" xsi:noNamespaceSchemaLocation="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n    <Header>\n        <ID>clarin.eu:cr1:c_1512742972470</ID>\n        <Name>DeletedComponent</Name>\n        <Description>For testing purposes. Private component that does not reference other components</Description>\n        <Status>development</Status>\n        <DerivedFrom>clarin.eu:cr1:c_1512740341362</DerivedFrom>\n    </Header>\n    <Component name="DeletedComponent" ConceptLink="http://test-concepts.org/standAloneSampleComponent" CardinalityMin="1" CardinalityMax="1">\n        <Element name="FirstElement" ConceptLink="http://test-concepts.org/firstElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="1" cue:DisplayPriority="1" xmlns:cue="http://www.clarin.eu/cmd/cues/1">\n            <Documentation>Test element</Documentation>\n        </Element>\n        <Element name="SecondElement" ConceptLink="http://test-concepts.org/secondElement" ValueScheme="string" CardinalityMin="0" CardinalityMax="unbounded">\n            <Documentation>Another test element, with an attribute</Documentation>\n            <AttributeList>\n                <Attribute name="SecondElementAttribute" ConceptLink="http://test-concepts.org/someAttribute" ValueScheme="int">\n                    <Documentation>An optional attribute on an element</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Element>\n        <Component name="FirstChildComponent" ConceptLink="http://test-concepts.org/firstComponent" CardinalityMin="1" CardinalityMax="unbounded">\n            <Documentation>First child component</Documentation>\n            <AttributeList>\n                <Attribute name="FirstChildComponentAttribute" ConceptLink="http://test-concepts.org/componentAttribute" ValueScheme="string" Required="true">\n                    <Documentation>Mandatory attribute on a component</Documentation>\n                </Attribute>\n            </AttributeList>\n        </Component>\n    </Component>\n</ComponentSpec>\n	0	clarin.eu:cr1:c_1512740341362	\N
\.


--
-- Name: basedescription_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('basedescription_id_seq', 11, true);


--
-- Data for Name: comments; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY comments (id, comments, comment_date, component_id, user_id, user_name) FROM stdin;
1	Test comment on public profile	2017-12-08 14:23:36.542+00	clarin.eu:cr1:p_1512739327528	1	The Administrator
3	Test comment on private component	2017-12-08 14:23:50.672+00	clarin.eu:cr1:c_1512740341362	1	The Administrator
4	Test comment on private profile	2017-12-08 14:23:58.458+00	clarin.eu:cr1:p_1512740341364	1	The Administrator
5	Test comment on public component	2017-12-08 14:24:09.938+00	clarin.eu:cr1:c_1512739327526	1	The Administrator
\.


--
-- Name: comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('comments_id_seq', 5, true);


--
-- Data for Name: groupmembership; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY groupmembership (id, groupid, userid) FROM stdin;
1	1	1
\.


--
-- Name: groupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('groupmembership_id_seq', 1, true);


--
-- Data for Name: itemlock; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY itemlock (id, itemid, userid, creationdate) FROM stdin;
\.


--
-- Name: itemlock_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('itemlock_id_seq', 2, false);


--
-- Data for Name: ownership; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY ownership (id, componentid, groupid, userid) FROM stdin;
1	clarin.eu:cr1:p_1512742972468	1	0
2	clarin.eu:cr1:c_1512742972469	1	0
\.


--
-- Name: ownership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('ownership_id_seq', 2, true);


--
-- Name: registry_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('registry_user_id_seq', 1, true);


--
-- Data for Name: usergroup; Type: TABLE DATA; Schema: public; Owner: compreg
--

COPY usergroup (id, ownerid, name) FROM stdin;
1	1	Test team
\.


--
-- Name: usergroup_id_seq; Type: SEQUENCE SET; Schema: public; Owner: compreg
--

SELECT pg_catalog.setval('usergroup_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

