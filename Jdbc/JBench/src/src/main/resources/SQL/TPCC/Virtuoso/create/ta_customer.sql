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
create table tpcc..customer (
    c_id		integer,
    c_d_id		integer,
    c_w_id		integer,
    c_first		character (16),
    c_middle		character (2),
    c_last		varchar,
    c_street_1		character (20),
    c_street_2		character (20),
    c_city		character (20),
    c_state		character (2),
    c_zip		character (9),
    c_phone		character (16),
    c_since		varchar,
    c_credit		character (2),
    c_credit_lim	numeric,
    c_discount		numeric,
    c_balance		numeric,
    c_ytd_payment	numeric,
    c_cnt_payment	integer,
    c_cnt_delivery	integer,
    c_data_1		character (250),
    c_data_2		character (250),
    primary key (c_w_id, c_d_id, c_id)
)
