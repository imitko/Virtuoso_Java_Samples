--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
-- 150 000 * SF
create table tpcd..customer (
		c_custkey		integer,
		c_name			varchar(25),
		c_address		varchar(40),
		c_nationkey		integer,
		c_phone			character(15),
		c_acctbal		numeric(20, 2),
		c_mktsegment	character(10),
		c_comment		varchar(117),
		primary key (c_custkey)
);

create table tpcd..history (
	H_P_KEY integer
	H_S_KEY integer
	H_O_KEY integer
	H_L_KEY integer
	H_DELTA integer
	H_DATE_T timestamp
);

-- 1 500 000 * random(1, 7) * SF
create table tpcd..lineitem (
		l_orderkey		integer,
		l_partkey		integer,
		l_suppkey		integer,
		l_linenumber	integer,
		l_quantity		numeric(20, 2),
		l_extendedprice	numeric(20, 2),
		l_discount		numeric(3, 2),
		l_tax			numeric(3, 2),
		l_returnflag	character(1),
		l_linestatus	character(1),
		l_shipdate		date,
		l_commitdate	date,
		l_receiptdate	date,
		l_shipinstruct	character(25),
		l_shipmode		character(10),
		l_comment		varchar(44),
		primary key (l_orderkey, l_linenumber)
);

-- 25
create table tpcd..nation (
		n_nationkey		integer,
		n_name			character(25),
		n_regionkey		integer,
		n_comment		varchar(152),
		primary key (n_nationkey)
);

-- 1 500 000 * SF
create table tpcd..orders (
		o_orderkey		integer,
		o_custkey		integer,
		o_orderstatus	character(1),
		o_totalprice	numeric(20, 2),
		o_orderdate		date,
		o_orderpriority	character(15),
		o_clerk			character(15),
		o_shippriority	integer,
		o_comment		varchar(79),
		primary key(o_orderkey)
);

-- 200 000 * SF
create table tpcd..part (
	p_partkey		integer,
	p_name			varchar(55),
	p_mfgr			character(55),
	p_brand			character(5),
	p_type			varchar(25),
	p_size			integer,
	p_container		character(10),
	p_retailprice	numeric(20, 2),
	p_comment		varchar(23),
	primary key (p_partkey)
);

-- 800 000 * SF
create table tpcd..partsupp (
		ps_partkey		integer,
		ps_suppkey		integer,
		ps_availqty		integer,
		ps_supplycost	numeric(20, 2),
		ps_comment		varchar(199),
		primary key (ps_partkey, ps_suppkey)
);

-- 5
create table tpcd..region (
		r_regionkey		integer,
		r_name			character(25),
		r_comment		varchar(152),
		primary key (r_regionkey)
);

-- 10 000 * SF
create table tpcd..supplier (
		s_suppkey		integer,
		s_name			character(25),
		s_address		varchar(40),
		s_nationkey		integer,
		s_phone			character(15),
		s_acctbal		numeric(20, 2),
		s_comment		varchar(101),
		primary key (s_suppkey)
);
