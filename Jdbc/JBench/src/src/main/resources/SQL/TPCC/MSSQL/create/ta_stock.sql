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
		    s_i_id          int,
		    s_w_id          smallint,
		    s_quantity      smallint,
		    s_dist_01       char(24),
		    s_dist_02       char(24),
		    s_dist_03       char(24),
		    s_dist_04       char(24),
		    s_dist_05       char(24),
		    s_dist_06       char(24),
		    s_dist_07       char(24),
		    s_dist_08       char(24),
		    s_dist_09       char(24),
		    s_dist_10       char(24),
		    s_ytd           int,
		    s_cnt_order     smallint,
		    s_cnt_remote    smallint,
		    s_data          char(50) )
