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
create table tpcc..stock (
    s_i_id		integer,
    s_w_id		integer,
    s_quantity		integer,
    s_dist_01		character (24),
    s_dist_02		character (24),
    s_dist_03		character (24),
    s_dist_04		character (24),
    s_dist_05		character (24),
    s_dist_06		character (24),
    s_dist_07		character (24),
    s_dist_08		character (24),
    s_dist_09		character (24),
    s_dist_10		character (24),
    s_ytd		numeric,
    s_cnt_order		integer,
    s_cnt_remote	integer,
    s_data		character (50),
    primary key (s_i_id, s_w_id)
)
