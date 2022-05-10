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
		       c_id           int,
		       c_d_id         tinyint,
		       c_w_id         smallint,
		       c_first        char(16),
		       c_middle       char(2),
		       c_last         char(16),
		       c_street_1     char(20),
		       c_street_2     char(20),
		       c_city         char(20),
		       c_state        char(2),
		       c_zip          char(9),
		       c_phone        char(16),
		       c_since        datetime,
		       c_credit       char(2),
		       c_credit_lim   float,
		       c_discount     float,
		       c_balance      float,
		       c_ytd_payment  float,
		       c_cnt_payment  smallint,
		       c_cnt_delivery smallint,
		       c_data_1       char(250),
		       c_data_2       char(250))
