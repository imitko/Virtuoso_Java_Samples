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
create proc slevel
       @w_id         smallint,
       @d_id         tinyint,
       @threshhold   smallint
as
    declare @o_id int

    select @o_id = d_next_o_id
        from tpcc..district
        where d_w_id = @w_id and d_id = @d_id

    select count(*) from tpcc..stock,
           (select distinct(ol_i_id) from tpcc..order_line
               where ol_w_id   = @w_id and
                     ol_d_id   = @d_id and
                     ol_o_id between (@o_id-20) and (@o_id-1)) OL

           where s_w_id     = @w_id and
                 s_i_id     = OL.ol_i_id and
                 s_quantity < @threshhold
